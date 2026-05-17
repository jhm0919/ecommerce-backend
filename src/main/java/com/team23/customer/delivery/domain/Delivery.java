package com.team23.customer.delivery.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries", indexes = @Index(name = "idx_delivery_order", columnList = "order_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;  // ★ Order Aggregate ID 참조 (1:1)

    @Embedded
    private Address address;

    @Embedded
    private Receiver receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(name = "tracking_number")
    private String trackingNumber;  // 운송장 번호 (배송 시작 후)

    @Column(name = "delivery_memo", length = 500)
    private String memo;  // "문 앞에 두세요" 등

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Delivery prepare(Long orderId, Address address, Receiver receiver, String memo) {
        Delivery delivery = new Delivery();
        delivery.orderId = orderId;
        delivery.address = address;
        delivery.receiver = receiver;
        delivery.memo = memo;
        delivery.status = DeliveryStatus.PREPARING;
        return delivery;
    }

    public void startShipping(String trackingNumber) {
        if (this.status != DeliveryStatus.PREPARING) {
            throw new IllegalStateException("Cannot start shipping in status: " + status);
        }
        this.trackingNumber = trackingNumber;
        this.status = DeliveryStatus.IN_TRANSIT;
        this.shippedAt = LocalDateTime.now();
    }

    public void completeDelivery() {
        if (this.status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException("Cannot complete delivery in status: " + status);
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = DeliveryStatus.FAILED;
    }
}
