package com.shop.notification.service;

import com.shop.notification.domain.Notification;
import com.shop.notification.domain.NotificationSourceType;
import com.shop.notification.repository.NotificationRepository;
import com.shop.admin.stockhistory.domain.StockChangeType;
import com.shop.admin.stockhistory.domain.StockHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCreatorServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private NotificationCreatorService notificationCreatorService;

    @Nested
    @DisplayName("품절 알림 생성")
    class CreateSoldOutNotification {

        @Test
        @DisplayName("재고가 1 이상에서 0으로 전이되면 알림 저장")
        void createWhenSoldOutTransition() {
            StockHistory history = history(1L, StockChangeType.ORDER, 3, 0);
            given(notificationRepository.existsBySourceTypeAndSourceId(
                    NotificationSourceType.STOCK_HISTORY, 1L)).willReturn(false);

            notificationCreatorService.createSoldOutNotificationIfNeeded(history);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification saved = captor.getValue();
            assertThat(saved.getSourceType()).isEqualTo(NotificationSourceType.STOCK_HISTORY);
            assertThat(saved.getSourceId()).isEqualTo(1L);
            assertThat(saved.getProductId()).isEqualTo(10L);
            assertThat(saved.getSkuId()).isEqualTo(100L);
            assertThat(saved.isRead()).isFalse();
        }

        @Test
        @DisplayName("이미 같은 StockHistory source 알림이 있으면 저장하지 않는다")
        void skipExistingSource() {
            StockHistory history = history(1L, StockChangeType.ORDER, 3, 0);
            given(notificationRepository.existsBySourceTypeAndSourceId(
                    NotificationSourceType.STOCK_HISTORY, 1L)).willReturn(true);

            notificationCreatorService.createSoldOutNotificationIfNeeded(history);

            verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("이미 품절 상태에서 0으로 남은 이력은 알림을 만들지 않는다")
        void skipZeroToZero() {
            StockHistory history = history(1L, StockChangeType.ORDER, 0, 0);

            notificationCreatorService.createSoldOutNotificationIfNeeded(history);

            verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("재고가 남아 있으면 알림을 만들지 않는다")
        void skipWhenStockRemains() {
            StockHistory history = history(1L, StockChangeType.ORDER, 5, 2);

            notificationCreatorService.createSoldOutNotificationIfNeeded(history);

            verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("어드민 증가 이력은 재고가 0이어도 품절 알림 대상이 아니다")
        void skipAdminIncrease() {
            StockHistory history = history(1L, StockChangeType.ADMIN_INCREASE, 3, 0);

            notificationCreatorService.createSoldOutNotificationIfNeeded(history);

            verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("StockHistory ID가 없으면 IllegalArgumentException")
        void rejectHistoryWithoutId() {
            StockHistory history = StockHistory.of(
                    10L, "티셔츠", 100L, "SKU-10-001", "색상=검정",
                    StockChangeType.ORDER, 3, 3, 0, 1000L
            );

            assertThatThrownBy(() ->
                    notificationCreatorService.createSoldOutNotificationIfNeeded(history))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("StockHistory id is required");

            verify(notificationRepository, never()).existsBySourceTypeAndSourceId(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any()
            );
            verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("exists=false 이후 save 유니크 충돌이 나도 source가 생겼으면 멱등 처리")
        void ignoreUniqueConflictAfterRace() {
            StockHistory history = history(1L, StockChangeType.ORDER, 3, 0);
            given(notificationRepository.existsBySourceTypeAndSourceId(
                    NotificationSourceType.STOCK_HISTORY, 1L))
                    .willReturn(false, true);
            given(notificationRepository.save(any(Notification.class)))
                    .willThrow(new DataIntegrityViolationException("unique conflict"));

            assertThatCode(() ->
                    notificationCreatorService.createSoldOutNotificationIfNeeded(history))
                    .doesNotThrowAnyException();

            verify(notificationRepository, times(2)).existsBySourceTypeAndSourceId(
                    NotificationSourceType.STOCK_HISTORY, 1L);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("저장 실패 후 source가 없으면 유니크 충돌이 아니므로 예외 재전파")
        void rethrowNonUniqueIntegrityViolation() {
            StockHistory history = history(1L, StockChangeType.ORDER, 3, 0);
            DataIntegrityViolationException exception =
                    new DataIntegrityViolationException("not null violation");
            given(notificationRepository.existsBySourceTypeAndSourceId(
                    NotificationSourceType.STOCK_HISTORY, 1L))
                    .willReturn(false, false);
            given(notificationRepository.save(any(Notification.class)))
                    .willThrow(exception);

            assertThatThrownBy(() ->
                    notificationCreatorService.createSoldOutNotificationIfNeeded(history))
                    .isSameAs(exception);
        }
    }

    private StockHistory history(
            Long id,
            StockChangeType changeType,
            int stockBefore,
            int stockAfter
    ) {
        StockHistory history = StockHistory.of(
                10L, "티셔츠", 100L, "SKU-10-001", "색상=검정",
                changeType, 3, stockBefore, stockAfter, 1000L
        );
        setId(history, id);
        return history;
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
