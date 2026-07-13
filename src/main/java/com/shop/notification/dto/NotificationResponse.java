package com.shop.notification.dto;

import com.shop.notification.domain.Notification;
import com.shop.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        Long productId,
        String productName,
        Long skuId,
        String skuCode,
        String skuOptionsSnapshot,
        boolean isRead,
        LocalDateTime occurredAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getProductId(),
                notification.getProductName(),
                notification.getSkuId(),
                notification.getSkuCode(),
                notification.getSkuOptionsSnapshot(),
                notification.isRead(),
                notification.getOccurredAt()
        );
    }
}
