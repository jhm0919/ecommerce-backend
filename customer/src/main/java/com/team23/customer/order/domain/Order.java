package com.team23.customer.order.domain;

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

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_member", columnList = "member_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_number", columnList = "order_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private OrderNumber orderNumber;

    @Column(name = "member_id", nullable = false)
    private Long memberId;  // ★ Member Aggregate ID 참조

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Order place(Long memberId, List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line");
        }
        Order order = new Order();
        order.orderNumber = OrderNumber.generate();
        order.memberId = memberId;
        order.status = OrderStatus.PENDING;
        order.orderedAt = LocalDateTime.now();
        lines.forEach(line -> {
            line.attachToOrder(order);
            order.orderLines.add(line);
        });
        order.totalAmount = order.calculateTotal();
        return order;
    }

    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be paid: current=" + status);
        }
        this.status = OrderStatus.PAID;
    }

    public void startPreparing() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("Order must be PAID first");
        }
        this.status = OrderStatus.PREPARING;
    }

    public void ship() {
        if (this.status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order must be PREPARING first");
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be SHIPPED first");
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (!this.status.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    private Money calculateTotal() {
        return orderLines.stream()
                .map(OrderLine::getSubtotal)
                .reduce(Money.ZERO_KRW, Money::add);
    }
}
