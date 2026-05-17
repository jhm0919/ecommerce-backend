package com.team23.customer.notification.service;

import com.team23.customer.notification.domain.Notification;
import com.team23.customer.notification.repository.NotificationRepository;
import com.team23.customer.product.domain.SkuSoldOutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도메인 이벤트 핸들러.
 * 재고 품절 이벤트를 받아 알림을 저장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationRepository notificationRepository;

    /**
     * SKU 품절 이벤트 처리.
     * 기존 트랜잭션에 참여 (주문 트랜잭션 내에서 함께 커밋).
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleSkuSoldOut(SkuSoldOutEvent event) {
        Notification notification = Notification.soldOut(
                event.productId(),
                event.productName(),
                event.skuId(),
                event.skuCode(),
                event.skuOptionsSnapshot()
        );
        notificationRepository.save(notification);

        log.info("Sold out notification created: productId={}, skuCode={}",
                event.productId(), event.skuCode());
    }
}
