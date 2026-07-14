package com.shop.notification.service;

import com.shop.notification.domain.Notification;
import com.shop.notification.domain.NotificationSourceType;
import com.shop.notification.repository.NotificationRepository;
import com.shop.admin.stock.domain.StockHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCreatorService {

    private static final NotificationSourceType STOCK_HISTORY =
            NotificationSourceType.STOCK_HISTORY;

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSoldOutNotificationIfNeeded(StockHistory history) {
        if (!history.isSoldOutTransition()) {
            return;
        }
        if (history.getId() == null) {
            throw new IllegalArgumentException("StockHistory id is required");
        }
        if (notificationRepository.existsBySourceTypeAndSourceId(
                STOCK_HISTORY, history.getId())) {
            return;
        }

        try {
            notificationRepository.save(Notification.soldOutFrom(history));
            log.info("Sold out notification created: stockHistoryId={}, skuCode={}",
                    history.getId(), history.getSkuCode());
        } catch (DataIntegrityViolationException e) {
            if (notificationRepository.existsBySourceTypeAndSourceId(
                    STOCK_HISTORY, history.getId())) {
                log.info("Sold out notification already exists: stockHistoryId={}",
                        history.getId());
                return;
            }
            throw e;
        }
    }
}
