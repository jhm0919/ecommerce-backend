package com.team23.customer.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 이커머스의 상품을 표현하는 Aggregate Root.
 *
 * <p>{@link Money} 값 객체로 가격을 표현하여 통화/금액의 안전성을 보장한다.
 * 상태({@link ProductStatus})는 ACTIVE → SOLD_OUT → DISCONTINUED 순서로 전이된다.
 *
 * <p>"삭제"는 Soft Delete 방식으로, status를 DISCONTINUED로 변경하여 처리한다.
 * 주문 이력 보존 등을 위해 물리적 삭제는 하지 않는다.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_created_at", columnList = "created_at")
})
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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money price;

    @Column(nullable = false)
    private int stock;

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
     *
     * <p>등록 직후 status는 stock에 따라 자동 결정된다:
     * <ul>
     *   <li>stock > 0 : ACTIVE</li>
     *   <li>stock == 0 : SOLD_OUT</li>
     * </ul>
     */
    public static Product register(
            String name,
            Money price,
            int stock,
            String description,
            String mainImageUrl,
            Category category
    ) {
        validateName(name);
        validatePrice(price);
        validateStock(stock);
        validateDescription(description);
        validateImageUrl(mainImageUrl);
        validateCategory(category);

        Product product = new Product();
        product.name = name.trim();
        product.price = price;
        product.stock = stock;
        product.description = (description == null) ? null : description.trim();
        product.mainImageUrl = mainImageUrl;
        product.category = category;
        product.status = (stock > 0) ? ProductStatus.ACTIVE : ProductStatus.SOLD_OUT;
        return product;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 정보 수정
    // ─────────────────────────────────────

    /**
     * 상품 정보를 부분 수정한다 (PATCH 의미).
     * null이 아닌 필드만 수정된다.
     */
    public void updateInfo(
            String name,
            String description,
            String mainImageUrl
    ) {
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
    }

    /**
     * 가격을 변경한다.
     * DISCONTINUED 상품은 가격 변경 불가.
     */
    public void changePrice(Money newPrice) {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot change price of discontinued product");
        }
        validatePrice(newPrice);
        this.price = newPrice;
    }

    /**
     * 카테고리를 변경한다.
     */
    public void changeCategory(Category newCategory) {
        validateCategory(newCategory);
        this.category = newCategory;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 재고
    // ─────────────────────────────────────

    /**
     * 재고를 증가시킨다 (입고).
     * SOLD_OUT 상태였다면 ACTIVE로 자동 전환.
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot restock discontinued product");
        }
        this.stock += quantity;
        if (this.status == ProductStatus.SOLD_OUT) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    /**
     * 재고를 감소시킨다 (출고/판매).
     * 재고가 0이 되면 SOLD_OUT으로 자동 전환.
     */
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        if (!this.status.isPurchasable()) {
            throw new IllegalStateException(
                    "Cannot decrease stock: product is not purchasable. Status: " + status);
        }
        if (this.stock < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Current: " + stock + ", requested: " + quantity);
        }
        this.stock -= quantity;
        if (this.stock == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 상태 전이
    // ─────────────────────────────────────

    /**
     * 상품을 단종 처리한다 (Soft Delete).
     * WITHDRAWN 상태는 영구적이며 되돌릴 수 없다.
     */
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

    public boolean isInStock() {
        return this.stock > 0;
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

    private static void validatePrice(Money price) {
        Objects.requireNonNull(price, "price must not be null");
        // Money 자체가 음수 거부하므로 추가 검증 불필요
    }

    private static void validateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative: " + stock);
        }
    }

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
}
