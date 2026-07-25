package com.shop.product.domain;

import com.shop.category.domain.Category;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 이커머스의 상품을 표현하는 Aggregate Root.
 *
 * 상태({@link ProductStatus})는 ACTIVE → SOLD_OUT → DISCONTINUED 순서로 전이된다.
 *
 * <p>재고는 SKU 단위로 관리된다. Product의 총 재고 = 모든 SKU 재고의 합계.
 *
 * <p>"삭제"는 Soft Delete 방식으로, status를 DISCONTINUED로 변경하여 처리한다.
 * 주문 이력 보존 등을 위해 물리적 삭제는 하지 않는다.
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;
    private static final int MAX_IMAGE_URL_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(length = MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(name = "main_image_url", length = MAX_IMAGE_URL_LENGTH)
    private String mainImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Sku> skus = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    /**
     * 새 상품을 등록한다.
     * 등록 직후 status는 ACTIVE.
     * 재고는 SKU 추가 후 SKU 단위로 관리된다.
     */
    public static Product register(
            String name,
            int price,
            String description,
            String mainImageUrl,
            Category category
    ) {

        validateName(name);
        validatePrice(price);
        validateDescription(description);
        validateImageUrl(mainImageUrl);
        validateCategory(category);

        Product product = new Product();
        product.name = name.trim();
        product.price = price;
        product.description = (description == null) ? null : description.trim();
        product.mainImageUrl = mainImageUrl;
        product.category = category;
        product.status = ProductStatus.ACTIVE;  // ★ 항상 ACTIVE로 시작
        return product;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 정보 수정 (변경 없음)
    // ─────────────────────────────────────

    public void update(String name,
                       String description,
                       String mainImageUrl,
                       Integer price,
                       Category category
    ) {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot update discontinued product");
        }

        if (name != null) {
            validateName(name);
            this.name = name.trim();
        }
        if (description != null) {
            validateDescription(description);
            this.description = description.trim();
        }
        if (mainImageUrl != null) {
            validateImageUrl(mainImageUrl);
            this.mainImageUrl = mainImageUrl;
        }
        if (price != null) {
            validatePrice(price);
            this.price = price;
        }
        if (category != null) {
            validateCategory(category);
            this.category = category;
        }
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 상태 전이 (변경 없음)
    // ─────────────────────────────────────

    public void discontinue() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Already discontinued");
        }
        this.status = ProductStatus.DISCONTINUED;
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    public boolean isPurchasable() {
        return this.status.isPurchasable();
    }

    public boolean isVisibleToCustomer() {
        return this.status.isVisibleToCustomer();
    }

    /**
     * 재고 여부 — 모든 SKU 합계 기준.
     */
    public boolean isInStock() {
        return getTotalSkuStock() > 0;  // ★ SKU 합계로 대체
    }

    // ─────────────────────────────────────
    // SKU 관리 (변경 없음)
    // ─────────────────────────────────────

    public Sku addSku(List<SkuOption> options, int initialStock) {
        if (this.id == null) {
            throw new IllegalStateException(
                    "Product must be persisted before adding SKUs");
        }
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot add SKU to discontinued product");
        }
        Objects.requireNonNull(options, "options must not be null");
        if (options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }

        boolean duplicate = skus.stream()
                .anyMatch(existing -> existing.hasSameOptions(options));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "SKU with same options already exists: " + options);
        }

        int sequence = skus.size() + 1;
        String skuCode = SkuCodeGenerator.generate(this.id, sequence);

        Sku sku = Sku.create(skuCode, options, initialStock);
        sku.assignToProduct(this);
        this.skus.add(sku);

        return sku;
    }

    public void decreaseSkuStock(Long skuId, int quantity) {
        Sku sku = findSku(skuId);
        sku.decreaseStock(quantity);

        boolean allEmpty = skus.stream().allMatch(s -> s.getStock() == 0);
        if (allEmpty) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    public void increaseSkuStock(Long skuId, int quantity) {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot restock discontinued product");
//            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        Sku sku = findSku(skuId);
        sku.increaseStock(quantity);

        if (this.status == ProductStatus.SOLD_OUT) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public void removeSku(Long skuId) {
        Sku sku = findSku(skuId);
        if (sku.getStock() > 0) {
            throw new IllegalStateException(
                    "Cannot remove SKU with stock: skuId=" + skuId);
        }
        this.skus.remove(sku);
    }

    public int getTotalSkuStock() {
        return skus.stream()
                .mapToInt(Sku::getStock)
                .sum();
    }

    public List<Sku> getSkuses() {
        return List.copyOf(skus);
    }

    public Optional<Sku> findSkuById(Long skuId) {
        return skus.stream()
                .filter(sku -> sku.getId() != null && sku.getId().equals(skuId))
                .findFirst();
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    private static void validatePrice(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }

    // ★ validateStock 제거

    private static void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    private static void validateImageUrl(String url) {
        if (url != null && url.length() > MAX_IMAGE_URL_LENGTH) {
            throw new IllegalArgumentException(
                    "image URL must not exceed " + MAX_IMAGE_URL_LENGTH + " characters");
        }
    }

    private static void validateCategory(Category category) {
        Objects.requireNonNull(category, "category must not be null");
    }

    private Sku findSku(Long skuId) {
        return skus.stream()
                .filter(sku -> sku.getId() != null && sku.getId().equals(skuId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "SKU not found: id=" + skuId));
    }
}