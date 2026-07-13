package com.shop.notification.domain;

import com.shop.admin.stockhistory.domain.StockChangeType;
import com.shop.admin.stockhistory.domain.StockHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("품절 알림 생성 시 기본값 확인")
    void createSoldOutNotification() {
        StockHistory history = soldOutHistory();
        setId(history, 500L);

        Notification notification = Notification.soldOutFrom(history);

        assertThat(notification.getType()).isEqualTo(NotificationType.SOLD_OUT);
        assertThat(notification.getProductId()).isEqualTo(1L);
        assertThat(notification.getProductName()).isEqualTo("티셔츠");
        assertThat(notification.getSkuId()).isEqualTo(100L);
        assertThat(notification.getSkuCode()).isEqualTo("SKU-1-001");
        assertThat(notification.getSkuOptionsSnapshot()).isEqualTo("색상=검정, 사이즈=S");
        assertThat(notification.getSourceType()).isEqualTo(NotificationSourceType.STOCK_HISTORY);
        assertThat(notification.getSourceId()).isEqualTo(500L);
        assertThat(notification.isRead()).isFalse();  // 기본 안읽음
    }

    @Test
    @DisplayName("읽음 처리")
    void markAsRead() {
        StockHistory history = soldOutHistory();
        setId(history, 500L);
        Notification notification = Notification.soldOutFrom(history);
        assertThat(notification.isRead()).isFalse();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("읽음 처리는 멱등 — 이미 읽어도 예외 없음")
    void markAsReadIdempotent() {
        StockHistory history = soldOutHistory();
        setId(history, 500L);
        Notification notification = Notification.soldOutFrom(history);
        notification.markAsRead();

        assertThatCode(notification::markAsRead)
                .doesNotThrowAnyException();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("StockHistory ID가 없으면 품절 알림을 생성할 수 없다")
    void rejectHistoryWithoutId() {
        StockHistory history = soldOutHistory();

        assertThatThrownBy(() -> Notification.soldOutFrom(history))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("stockHistory.id must not be null");
    }

    private StockHistory soldOutHistory() {
        return StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정, 사이즈=S",
                StockChangeType.ORDER, 3, 3, 0, 1000L
        );
    }

    private static void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
