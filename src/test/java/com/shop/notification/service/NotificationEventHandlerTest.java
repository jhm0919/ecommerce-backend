package com.shop.notification.service;

import com.shop.admin.stock.domain.StockType;
import com.shop.admin.stock.domain.StockHistory;
import com.shop.admin.stock.domain.StockHistoryRecordedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock private NotificationCreatorService notificationCreatorService;

    @InjectMocks private NotificationEventHandler notificationEventHandler;

    @Test
    @DisplayName("재고 이력 저장 이벤트 수신 시 알림 생성 서비스에 위임")
    void handleStockHistoryRecorded() {
        StockHistory history = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정, 사이즈=S",
                StockType.ORDER, 3, 3, 0, 1001L
        );

        notificationEventHandler.handleStockHistoryRecorded(
                new StockHistoryRecordedEvent(history));

        verify(notificationCreatorService).createSoldOutNotificationIfNeeded(history);
    }

    @Test
    @DisplayName("알림 생성 서비스가 실패하면 예외를 전파한다")
    void propagateCreatorException() {
        StockHistory history = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정, 사이즈=S",
                StockType.ORDER, 3, 3, 0, 1001L
        );
        RuntimeException exception = new RuntimeException("notification failed");
        willThrow(exception).given(notificationCreatorService)
                .createSoldOutNotificationIfNeeded(history);

        assertThatThrownBy(() -> notificationEventHandler.handleStockHistoryRecorded(
                new StockHistoryRecordedEvent(history)))
                .isSameAs(exception);
    }
}
