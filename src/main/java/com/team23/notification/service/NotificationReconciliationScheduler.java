package com.team23.notification.service;

import com.team23.admin.stockhistory.domain.StockChangeType;
import com.team23.admin.stockhistory.domain.StockHistory;
import com.team23.admin.stockhistory.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationReconciliationScheduler {

    private static final List<StockChangeType> SOLD_OUT_CHANGE_TYPES = List.of(
            StockChangeType.ORDER,
            StockChangeType.ADMIN_DECREASE
    );

    private final StockHistoryRepository stockHistoryRepository;
    private final NotificationCreatorService notificationCreatorService;

    @Scheduled(
            fixedDelayString = "${app.notifications.reconciliation.fixed-delay-ms:300000}",
            initialDelayString = "${app.notifications.reconciliation.initial-delay-ms:60000}"
    )
    public void reconcileSoldOutNotifications() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        int checkedCount = 0;
        Pageable pageable = PageRequest.of(0, 500);

        Page<StockHistory> histories;
        do {
            histories = stockHistoryRepository.findSoldOutTransitionsSince(
                    SOLD_OUT_CHANGE_TYPES,
                    from,
                    pageable
            );

            histories.forEach(notificationCreatorService::createSoldOutNotificationIfNeeded);
            checkedCount += histories.getNumberOfElements();
            pageable = histories.nextPageable();
        } while (histories.hasNext());

        if (checkedCount > 0) {
            log.info("Sold out notification reconciliation checked {} histories",
                    checkedCount);
        }
    }
}
