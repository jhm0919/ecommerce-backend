package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Product;
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
import java.util.Optional;

/**
 * 회원의 장바구니를 표현하는 Aggregate Root.
 *
 * <p>한 회원당 하나의 장바구니 (1:1).
 * 비회원은 클라이언트의 LocalStorage에서 관리되며 서버에 저장되지 않는다.
 *
 * <p>장바구니 항목의 가격 정보는 보관하지 않으며, 조회 시점에 Product의 현재 가격을 사용한다.
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_member", columnList = "member_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true, updatable = false)
    private Long memberId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

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
     * 회원의 빈 장바구니를 생성한다.
     */
    public static Cart createFor(Long memberId) {
        Objects.requireNonNull(memberId, "memberId must not be null");

        Cart cart = new Cart();
        cart.memberId = memberId;
        return cart;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 항목 관리
    // ─────────────────────────────────────

    /**
     * 장바구니에 상품을 추가한다.
     * 같은 상품이 이미 있으면 수량을 증가시킨다.
     */
    public void addItem(Product product, int quantity) {
        Objects.requireNonNull(product, "product must not be null");

        Optional<CartItem> existing = findItemByProductId(product.getId());

        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
        } else {
            CartItem newItem = CartItem.of(product, quantity);
            newItem.assignToCart(this);
            this.items.add(newItem);
        }
    }

    /**
     * 특정 항목의 수량을 변경한다.
     * 항목 ID 기준 (productId 아님 — 한 카트에 같은 상품 두 항목 가능성 차단되어 있지만 명시적으로).
     */
    public void changeItemQuantity(Long itemId, int newQuantity) {
        CartItem item = findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cart item not found: id=" + itemId));
        item.changeQuantity(newQuantity);
    }

    /**
     * 특정 항목을 제거한다.
     */
    public void removeItem(Long itemId) {
        CartItem item = findItemById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cart item not found: id=" + itemId));
        this.items.remove(item);
    }

    /**
     * 장바구니를 비운다.
     */
    public void clear() {
        this.items.clear();
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    /**
     * 장바구니가 비어 있는지 확인.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 장바구니의 총 항목 종류 수.
     * (수량 합계가 아니라 종류 수)
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * 장바구니의 총 수량 합계.
     */
    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * 외부에 노출할 항목 목록 (불변).
     */
    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    // ─────────────────────────────────────
    // 헬퍼 메서드 (private)
    // ─────────────────────────────────────

    private Optional<CartItem> findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private Optional<CartItem> findItemById(Long itemId) {
        return items.stream()
                .filter(item -> item.getId() != null && item.getId().equals(itemId))
                .findFirst();
    }
}