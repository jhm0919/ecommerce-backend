package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Money;
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
import java.util.Optional;

@Entity
@Table(name = "carts", indexes = @Index(name = "idx_cart_member", columnList = "member_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;  // ★ Member Aggregate ID 참조 (1인 1카트)

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Cart createFor(Long memberId) {
        Cart cart = new Cart();
        cart.memberId = memberId;
        return cart;
    }

    /**
     * 장바구니에 상품 추가. 이미 있으면 수량만 증가.
     */
    public void addItem(Long productId, String productName, Money unitPrice, int quantity) {
        Optional<CartItem> existing = findItemByProductId(productId);
        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
        } else {
            CartItem newItem = CartItem.of(this, productId, productName, unitPrice, quantity);
            this.items.add(newItem);
        }
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void changeItemQuantity(Long productId, int quantity) {
        CartItem item = findItemByProductId(productId)
                .orElseThrow(() -> new IllegalStateException("Item not in cart: " + productId));
        item.changeQuantity(quantity);
    }

    public void clear() {
        this.items.clear();
    }

    public Money getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(Money.ZERO_KRW, Money::add);
    }

    public int getItemCount() {
        return items.size();
    }

    private Optional<CartItem> findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }
}