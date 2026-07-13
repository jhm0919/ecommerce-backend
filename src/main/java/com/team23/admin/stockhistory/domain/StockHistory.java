package com.team23.admin.stockhistory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 재고 변동 이력.
 *
 * <p>모든 재고 변동(주문/취소/어드민)을 기록한다.
 * FK 없이 ID만 보관 (이력은 삭제 없이 영구 보존).
 */
@Entity
@Table(name = "stock_histories", indexes = {
        @Index(name = "idx_stock_history_product", columnList = "product_id"),
        @Index(name = "idx_stock_history_sku", columnList = "sku_id"),
        @Index(name = "idx_stock_history_occurred_at", columnList = "occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(name = "sku_options_snapshot", length = 500)
    private String skuOptionsSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private StockChangeType changeType;

    @Column(nullable = false)
    private int quantity;       // 변동량 (양수)

    @Column(name = "stock_before", nullable = false)
    private int stockBefore;

    @Column(name = "stock_after", nullable = false)
    private int stockAfter;

    @Column(name = "order_id")
    private Long orderId;       // nullable

    @CreatedDate
    @Column(name = "occurred_at", updatable = false, nullable = false)
    private LocalDateTime occurredAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static StockHistory of(
            Long productId,
            String productName,
            Long skuId,
            String skuCode,
            String skuOptionsSnapshot,
            StockChangeType changeType,
            int quantity,
            int stockBefore,
            int stockAfter,
            Long orderId
    ) {
        StockHistory history = new StockHistory();
        history.productId = productId;
        history.productName = productName;
        history.skuId = skuId;
        history.skuCode = skuCode;
        history.skuOptionsSnapshot = skuOptionsSnapshot;
        history.changeType = changeType;
        history.quantity = quantity;
        history.stockBefore = stockBefore;
        history.stockAfter = stockAfter;
        history.orderId = orderId;
        return history;
    }

    public boolean isSoldOutTransition() {
        return (changeType == StockChangeType.ORDER
                || changeType == StockChangeType.ADMIN_DECREASE)
                && stockBefore > 0
                && stockAfter == 0;
    }
}
