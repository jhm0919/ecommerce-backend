package com.team23.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stock Keeping Unit — 재고 관리 단위.
 *
 * <p>한 Product의 옵션 조합(색상, 사이즈 등)으로 구분되는 개별 재고 단위.
 * Product Aggregate에 속하며, Product를 통해서만 관리된다.
 */
@Entity
@Table(name = "skus", indexes = {
        @Index(name = "idx_sku_code", columnList = "sku_code", unique = true),
        @Index(name = "idx_sku_product", columnList = "product_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku_code", nullable = false, unique = true, length = 50, updatable = false)
    private String skuCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "sku_options",
            joinColumns = @JoinColumn(name = "sku_id")
    )
    @OrderColumn(name = "option_order")
    private List<SkuOption> options = new ArrayList<>();

    @Column(nullable = false)
    private int stock;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리 (package-private)
    // ─────────────────────────────────────

    /**
     * SKU를 생성한다. Product에서만 호출되어야 한다.
     */
    static Sku create(String skuCode, List<SkuOption> options, int stock) {
        Objects.requireNonNull(skuCode, "skuCode must not be null");
        Objects.requireNonNull(options, "options must not be null");
        if (options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative: " + stock);
        }

        Sku sku = new Sku();
        sku.skuCode = skuCode;
        sku.options = new ArrayList<>(options);
        sku.stock = stock;
        return sku;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 (package-private)
    // ─────────────────────────────────────

    void assignToProduct(Product product) {
        this.product = product;
    }

    /**
     * 재고를 증가시킨다.
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        this.stock += quantity;
    }

    /**
     * 재고를 감소시킨다.
     */
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        if (this.stock < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Current: " + stock + ", requested: " + quantity);
        }
        this.stock -= quantity;
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    /**
     * 같은 옵션 조합인지 확인.
     * 옵션 순서/중복 무관.
     */
    public boolean hasSameOptions(List<SkuOption> other) {
        if (this.options.size() != other.size()) {
            return false;
        }
        // 양쪽 모두 포함하는지
        return this.options.containsAll(other) && other.containsAll(this.options);
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public List<SkuOption> getOptions() {
        return List.copyOf(options);
    }
}
