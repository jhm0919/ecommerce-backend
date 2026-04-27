package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;  // ★ Product Aggregate ID 참조

    @Column(name = "product_name", nullable = false)
    private String productName;  // 스냅샷 (UX용)

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;  // 스냅샷 (담은 시점 가격)

    @Column(nullable = false)
    private int quantity;

    static CartItem of(Cart cart, Long productId, String productName, Money unitPrice, int quantity) {
        validateQuantity(quantity);
        CartItem item = new CartItem();
        item.cart = cart;
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice;
        item.quantity = quantity;
        return item;
    }

    void increaseQuantity(int delta) {
        validateQuantity(this.quantity + delta);
        this.quantity += delta;
    }

    void changeQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (quantity > 100) {  // 비즈니스 룰
            throw new IllegalArgumentException("Quantity cannot exceed 100");
        }
    }
}