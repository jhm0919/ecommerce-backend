package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.Sku;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 장바구니에 담긴 개별 상품 항목.
 *
 * <p>Cart Aggregate의 부속 엔티티. Cart를 통해서만 접근/관리된다.
 *
 * <p>가격 정보는 보관하지 않는다 (조회 시 현재 Product 가격 사용).
 * 이름과 이미지는 스냅샷으로 보관 (단종된 상품도 표시 가능).
 * SKU 옵션 정보는 보관하지 않는다 (조회 시 SKU 참조).
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_product", columnList = "product_id"),
        @Index(name = "idx_cart_item_sku", columnList = "sku_id")  // ★ 인덱스 추가
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_IMAGE_URL_LENGTH = 500;
    private static final int MAX_QUANTITY = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "sku_id", nullable = false, updatable = false)
    private Long skuId;  // ★ product_id 바로 아래로 이동 (논리적 그룹)

    @Column(name = "product_name", nullable = false, length = MAX_NAME_LENGTH)
    private String productName;

    @Column(name = "product_image_url", length = MAX_IMAGE_URL_LENGTH)
    private String productImageUrl;

    @Column(nullable = false)
    private int quantity;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    /**
     * Product, SKU, 수량으로부터 CartItem을 생성한다.
     *
     * <p>SKU 옵션 정보는 저장하지 않는다.
     * 조회 시 skuId로 SKU를 참조해 옵션을 표시한다.
     */
    static CartItem of(Product product, Sku sku, int quantity) {
        Objects.requireNonNull(product, "product must not be null");
        Objects.requireNonNull(sku, "sku must not be null");
        validateQuantity(quantity);

        CartItem item = new CartItem();
        item.productId = product.getId();
        item.skuId = sku.getId();            // ★ 설정
        item.productName = product.getName();
        item.productImageUrl = product.getMainImageUrl();
        item.quantity = quantity;
        return item;
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    /**
     * 같은 SKU인지 확인.
     * Cart.addItem의 합산 조건으로 사용된다.
     *
     * <p>같은 Product라도 옵션(SKU)이 다르면 별도 항목으로 관리된다.
     */
    public boolean isSameSku(Long skuId) {
        return this.skuId.equals(skuId);
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 (package-private)
    // ─────────────────────────────────────

    void increaseQuantity(int delta) {
        int newQuantity = this.quantity + delta;
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    void changeQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    void assignToCart(Cart cart) {
        this.cart = cart;
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be positive: " + quantity);
        }
        if (quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "quantity must not exceed " + MAX_QUANTITY + ": " + quantity);
        }
    }
}