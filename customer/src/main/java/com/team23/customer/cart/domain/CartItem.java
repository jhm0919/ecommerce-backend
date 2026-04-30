package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Product;
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
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_product", columnList = "product_id")
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
     * Product와 수량으로부터 CartItem을 생성한다.
     */
    static CartItem of(Product product, int quantity) {
        Objects.requireNonNull(product, "product must not be null");
        validateQuantity(quantity);

        CartItem item = new CartItem();
        item.productId = product.getId();
        item.productName = product.getName();
        item.productImageUrl = product.getMainImageUrl();
        item.quantity = quantity;
        return item;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 (package-private)
    // ─────────────────────────────────────

    /**
     * 수량을 증가시킨다.
     * Cart.addItem에서만 호출되어야 한다.
     */
    void increaseQuantity(int delta) {
        int newQuantity = this.quantity + delta;
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    /**
     * 수량을 변경한다 (덮어쓰기).
     * Cart.changeItemQuantity에서만 호출되어야 한다.
     */
    void changeQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    /**
     * Cart와의 관계를 설정한다.
     * Cart.addItem에서만 호출되어야 한다.
     */
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