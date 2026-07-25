package com.shop.order.domain;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.product.domain.Money;
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
 * 주문을 표현하는 Aggregate Root.
 *
 * <p>회원 주문과 비회원 주문을 모두 지원한다:
 * <ul>
 *   <li>회원 주문: {@code memberId}가 NOT NULL</li>
 * </ul>
 *
 * <p>배송 정보(받는 사람, 주소, 배송 상태 등)는 별도 {@code Delivery} Aggregate에서 관리한다.
 * Order는 주문의 비즈니스 측면(상품, 금액, 결제 상태)에만 집중한다.
 *
 * <p>Order ↔ Delivery 관계는 ID 참조 (Delivery.orderId).
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, updatable = false, length = 30)
    private String orderNumber;

    @Column(name = "member_id", updatable = false)
    private Long memberId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)),
    })
    private Money totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리 - 회원 주문
    // ─────────────────────────────────────

    /**
     * 회원 주문을 생성한다.
     * 배송 정보는 별도 Delivery Aggregate에서 처리한다.
     */
    public static Order createForMember(Long memberId, List<OrderItem> items) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        return create(memberId, items);
    }

    /**
     * 공통 생성 로직.
     */
    private static Order create(
            Long memberId,
            List<OrderItem> items
    ) {
        validateItems(items);

        Order order = new Order();
        order.orderNumber = OrderNumberGenerator.generate();
        order.memberId = memberId;
        order.status = OrderStatus.PENDING;

        // OrderItem들 추가 + 양방향 관계 설정
        for (OrderItem item : items) {
            order.addItem(item);
        }

        // 총 금액 계산
        order.totalAmount = order.calculateTotal();

        return order;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 항목 관리
    // ─────────────────────────────────────

    private void addItem(OrderItem item) {
        Objects.requireNonNull(item, "item must not be null");
        item.assignToOrder(this);
        this.items.add(item);
    }

    private Money calculateTotal() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot calculate total of empty order");
        }

        Money total = items.get(0).calculateSubtotal();
        for (int i = 1; i < items.size(); i++) {
            total = total.add(items.get(i).calculateSubtotal());
        }
        return total;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드 - 상태 전이
    // ─────────────────────────────────────

    /**
     * 주문을 취소한다.
     * 배송이 시작되기 전(PENDING)에만 가능.
     *
     * <p>주의: 실제 배송 시작 여부는 Delivery 상태로 판단되므로,
     * Service 레벨에서 Delivery.status도 함께 확인해야 한다.
     */
    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot cancel: order is not pending. Current: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    public boolean isGuestOrder() {
        return memberId == null;
    }

    public boolean isMemberOrder() {
        return memberId != null;
    }

    /**
     * 외부에 노출할 항목 목록 (불변).
     */
    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }


    /**
     *  (Admin) 상태 변경 메서드
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void forceCancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        this.status = OrderStatus.CANCELLED;
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateItems(List<OrderItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }

    private static void validateGuestEmail(String email) {
        Objects.requireNonNull(email, "guest email must not be null");
        if (email.isBlank()) {
            throw new IllegalArgumentException("guest email must not be blank");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private static void validateGuestPhone(String phone) {
        Objects.requireNonNull(phone, "guest phone must not be null");
        if (phone.isBlank()) {
            throw new IllegalArgumentException("guest phone must not be blank");
        }
    }
}