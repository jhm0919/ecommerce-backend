package com.shop.order.domain;

import com.shop.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.shop.product.domain.Sku;       // ★ 추가
import com.shop.product.domain.SkuOption; // ★ 추가

import java.util.ArrayList;  // ★ 추가
import java.util.List;        // ★ 추가

import java.util.Objects;

/**
 * 주문에 포함된 개별 상품 항목.
 *
 * <p>Order Aggregate의 부속 엔티티. Order를 통해서만 접근/관리된다.
 *
 * <p>주문 시점의 상품 정보를 스냅샷으로 저장:
 * <ul>
 *   <li>productId — 원본 상품 참조 (정보용, 다른 Aggregate)</li>
 *   <li>productName, priceAtOrder, productImageUrl — 주문 시점 정보 영구 보존</li>
 * </ul>
 *
 * <p>이를 통해 상품 가격/이름이 변경되거나 단종되어도 주문 내역은 정확히 보존된다.
 */
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_product", columnList = "product_id"),
        @Index(name = "idx_order_item_sku", columnList = "sku_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_IMAGE_URL_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, updatable = false, length = MAX_NAME_LENGTH)
    private String productName;

    @Column(name = "product_image_url", updatable = false, length = MAX_IMAGE_URL_LENGTH)
    private String productImageUrl;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, updatable = false)
    private int quantity;

    // ─── SKU 정보 (스냅샷) ───

    @Column(name = "sku_id", nullable = false, updatable = false)
    private Long skuId;

    @Column(name = "sku_code", nullable = false, updatable = false)
    private String skuCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "order_item_sku_options",
            joinColumns = @JoinColumn(name = "order_item_id")
    )
    @OrderColumn(name = "option_order")
    private List<SkuOption> skuOptions = new ArrayList<>();


    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    /**
     * Product와 수량으로부터 OrderItem을 생성한다.
     * 상품 정보를 스냅샷으로 복사한다.
     */
    /**
     * Product와 SKU로부터 OrderItem 생성.
     *
     * <p>주의: SKU가 Product에 속한 것인지 호출자가 검증해야 한다.
     * 일반적으로 Product에서 SKU를 조회한 후 호출.
     */
    public static OrderItem of(Product product, Sku sku, int quantity) {
        Objects.requireNonNull(product, "product must not be null");
        Objects.requireNonNull(sku, "sku must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }

        OrderItem item = new OrderItem();
        item.productId = product.getId();
        item.productName = product.getName();
        item.productImageUrl = product.getMainImageUrl();

        item.skuId = sku.getId();
        item.skuCode = sku.getSkuCode();
        item.skuOptions = new ArrayList<>(sku.getOptions());

        item.price = product.getPrice();
        item.quantity = quantity;
        return item;
    }

    public List<SkuOption> getSkuOptions() {
        return List.copyOf(skuOptions);
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    /**
     * 이 항목의 소계 (단가 × 수량).
     */
//    public Money calculateSubtotal() {
//        return price.multiply(quantity);
//    }

    /**
     * Order와의 관계를 설정한다.
     * Order.addItem에서만 호출되어야 한다.
     */
    void assignToOrder(Order order) {
        this.order = order;
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
    }

    public int calculateSubtotal() {
        return Math.multiplyExact(price, quantity);
    }


}