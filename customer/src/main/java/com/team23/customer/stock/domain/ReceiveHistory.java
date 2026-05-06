package com.team23.customer.stock.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 재고 변동 이력.
 * 입고 처리 시 생성되며 재고 변동 내역을 추적한다.
 */
@Entity
@Table(name = "receive_histories", indexes = {
        @Index(name = "idx_sh_sku", columnList = "sku_id"),
        @Index(name = "idx_sh_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ReceiveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_id", nullable = false, updatable = false)
    private Long skuId;

    @Column(name = "purchase_order_id", updatable = false)
    private Long purchaseOrderId;   // 발주 기반 입고 시

    @Column(name = "received_quantity", nullable = false, updatable = false)
    private int receivedQuantity;   // 이번 입고 수량

    @Column(name = "stock_after", nullable = false, updatable = false)
    private int stockAfter;         // 입고 후 재고

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static ReceiveHistory of(
            Long skuId,
            Long purchaseOrderId,
            int receivedQuantity,
            int stockAfter
    ) {
        ReceiveHistory history = new ReceiveHistory();
        history.skuId = skuId;
        history.purchaseOrderId = purchaseOrderId;
        history.receivedQuantity = receivedQuantity;
        history.stockAfter = stockAfter;
        return history;
    }
}
