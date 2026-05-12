package com.team23.customer.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_settlement_order", columnList = "order_id", unique = true),
        @Index(name = "idx_settlement_month", columnList = "settled_month")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, updatable = false)
    private Long orderId;

    @Column(name = "order_amount", nullable = false, updatable = false)
    private BigDecimal orderAmount;

    @Column(nullable = false, updatable = false)
    private BigDecimal fee;

    @Column(name = "settlement_amount", nullable = false, updatable = false)
    private BigDecimal settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "settled_month", nullable = false, updatable = false, length = 7)
    private String settledMonth;   // "2026-05"

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static Settlement confirm(
            Long orderId,
            BigDecimal orderAmount,
            BigDecimal fee,
            BigDecimal settlementAmount,
            YearMonth settledMonth
    ) {
        Settlement s = new Settlement();
        s.orderId = orderId;
        s.orderAmount = orderAmount;
        s.fee = fee;
        s.settlementAmount = settlementAmount;
        s.status = SettlementStatus.CONFIRMED;
        s.settledMonth = settledMonth.toString();   // "2026-05"
        return s;
    }
}
