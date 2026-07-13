package com.team23.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.team23.admin.stockhistory.domain.StockHistoryRecordedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트 핸들러.
 * 커밋된 재고 이력 이벤트를 받아 알림을 저장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationCreatorService notificationCreatorService;

    /**
     * 재고 이력 커밋 후 비동기로 품절 알림을 생성한다.
     */
    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockHistoryRecorded(StockHistoryRecordedEvent event) {
        notificationCreatorService.createSoldOutNotificationIfNeeded(event.stockHistory());
    }
}
