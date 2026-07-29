package com.shop.product.domain;

import com.shop.order.exception.InsufficientStockException;
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
@Table(name = "skus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 연관관계 주인, DB에 넣거나 수정할때 여기를 참조, FK
    @JoinColumn(name = "product_ id", nullable = false)
    private Product product;

    @Column(name = "sku_code", nullable = false, unique = true, length = 50, updatable = false)
    private String skuCode;

    @ElementCollection
    @CollectionTable(
            name = "sku_options", // 테이블 이름
            joinColumns = @JoinColumn(name = "sku_id")
    ) // 값 타입 컬렉션
    private List<SkuOption> options = new ArrayList<>();

    @Column(nullable = false)
    private int quantity;

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
        sku.quantity = stock;
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
     void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        this.quantity += quantity;
    }

    /**
     * 재고를 감소시킨다.
     */
     void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        if (this.quantity < quantity) {
            throw new InsufficientStockException(this.quantity, quantity);
        }
        this.quantity -= quantity;
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
        return quantity > 0;
    }

    public List<SkuOption> getOptions() {
        return List.copyOf(options);
    }
}
