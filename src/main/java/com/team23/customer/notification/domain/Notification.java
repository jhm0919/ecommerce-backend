package com.team23.customer.notification.domain;

import com.team23.customer.stockhistory.domain.StockHistory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 어드민 알림.
 *
 * <p>SKU 품절 등 운영 관련 이벤트를 기록한다.
 * 참조 데이터(productId, skuId)는 FK 없이 ID만 보관 (알림은 과거 기록 보존 목적).
 */
@Entity
@Table(name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_source",
                        columnNames = {"source_type", "source_id"}
                )
        },
        indexes = {
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
        @Index(name = "idx_notification_occurred_at", columnList = "occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    // ─── 참조 정보 (FK 없음 — 이력 보존) ───

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(name = "sku_options_snapshot", length = 500)
    private String skuOptionsSnapshot;  // "색상=검정, 사이즈=S"

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private NotificationSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    // ─── 상태 ───

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "occurred_at", updatable = false, nullable = false)
    private LocalDateTime occurredAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static Notification soldOut(
            Long productId,
            String productName,
            Long skuId,
            String skuCode,
            String skuOptionsSnapshot,
            NotificationSourceType sourceType,
            Long sourceId
    ) {
        Notification notification = new Notification();
        notification.type = NotificationType.SOLD_OUT;
        notification.productId = productId;
        notification.productName = productName;
        notification.skuId = skuId;
        notification.skuCode = skuCode;
        notification.skuOptionsSnapshot = skuOptionsSnapshot;
        notification.sourceType = sourceType;
        notification.sourceId = sourceId;
        notification.isRead = false;
        return notification;
    }

    public static Notification soldOutFrom(StockHistory history) {
        Objects.requireNonNull(history, "stockHistory must not be null");
        Long sourceId = Objects.requireNonNull(
                history.getId(),
                "stockHistory.id must not be null"
        );

        return soldOut(
                history.getProductId(),
                history.getProductName(),
                history.getSkuId(),
                history.getSkuCode(),
                history.getSkuOptionsSnapshot(),
                NotificationSourceType.STOCK_HISTORY,
                sourceId
        );
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    /**
     * 읽음 처리.
     * 이미 읽은 알림은 무동작 (멱등).
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
