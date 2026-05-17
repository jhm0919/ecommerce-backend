package com.team23.customer.notification.service;

import com.team23.customer.notification.domain.Notification;
import com.team23.customer.notification.repository.NotificationRepository;
import com.team23.customer.product.domain.SkuSoldOutEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private NotificationEventHandler notificationEventHandler;

    @Test
    @DisplayName("SKU 품절 이벤트 수신 시 알림 저장")
    void handleSkuSoldOut() {
        SkuSoldOutEvent event = new SkuSoldOutEvent(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정, 사이즈=S"
        );

        notificationEventHandler.handleSkuSoldOut(event);

        // 저장된 알림 내용 검증
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getProductId()).isEqualTo(1L);
        assertThat(saved.getProductName()).isEqualTo("티셔츠");
        assertThat(saved.getSkuId()).isEqualTo(100L);
        assertThat(saved.getSkuCode()).isEqualTo("SKU-1-001");
        assertThat(saved.getSkuOptionsSnapshot()).isEqualTo("색상=검정, 사이즈=S");
        assertThat(saved.isRead()).isFalse();
    }
}