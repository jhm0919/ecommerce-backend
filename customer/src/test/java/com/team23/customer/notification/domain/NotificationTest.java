package com.team23.customer.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("품절 알림 생성 시 기본값 확인")
    void createSoldOutNotification() {
        Notification notification = Notification.soldOut(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정, 사이즈=S"
        );

        assertThat(notification.getType()).isEqualTo(NotificationType.SOLD_OUT);
        assertThat(notification.getProductId()).isEqualTo(1L);
        assertThat(notification.getProductName()).isEqualTo("티셔츠");
        assertThat(notification.getSkuId()).isEqualTo(100L);
        assertThat(notification.getSkuCode()).isEqualTo("SKU-1-001");
        assertThat(notification.getSkuOptionsSnapshot()).isEqualTo("색상=검정, 사이즈=S");
        assertThat(notification.isRead()).isFalse();  // 기본 안읽음
    }

    @Test
    @DisplayName("읽음 처리")
    void markAsRead() {
        Notification notification = Notification.soldOut(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정"
        );
        assertThat(notification.isRead()).isFalse();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("읽음 처리는 멱등 — 이미 읽어도 예외 없음")
    void markAsReadIdempotent() {
        Notification notification = Notification.soldOut(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정"
        );
        notification.markAsRead();

        assertThatCode(notification::markAsRead)
                .doesNotThrowAnyException();

        assertThat(notification.isRead()).isTrue();
    }
}