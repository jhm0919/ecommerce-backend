package com.shop.notification.service;

import com.shop.notification.domain.Notification;
import com.shop.notification.repository.NotificationRepository;
import com.shop.admin.stockhistory.domain.StockChangeType;
import com.shop.admin.stockhistory.domain.StockHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private NotificationService notificationService;

    private Notification unreadNotification;
    private Notification readNotification;

    @BeforeEach
    void setUp() {
        unreadNotification = Notification.soldOutFrom(soldOutHistory(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정, 사이즈=S"));
        readNotification = Notification.soldOutFrom(soldOutHistory(
                2L, "바지", 200L, "SKU-2-001", "색상=회색"));
        readNotification.markAsRead();
    }

    private StockHistory soldOutHistory(
            Long productId,
            String productName,
            Long skuId,
            String skuCode,
            String skuOptionsSnapshot
    ) {
        StockHistory history = StockHistory.of(
                productId, productName, skuId, skuCode, skuOptionsSnapshot,
                StockChangeType.ORDER, 1, 1, 0, 1000L
        );
        setId(history, skuId + 1000);
        return history;
    }

    private static void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("전체 알림 목록 조회")
    class FindAll {

        @Test
        @DisplayName("최신순으로 알림 목록을 반환한다")
        void findAllNotifications() {
            Pageable pageable = PageRequest.of(0, 20);
            given(notificationRepository.findAllByOrderByOccurredAtDesc(pageable))
                    .willReturn(new PageImpl<>(List.of(unreadNotification, readNotification)));

            Page<Notification> result = notificationService.findAll(pageable);

            assertThat(result.getContent()).hasSize(2);
            verify(notificationRepository).findAllByOrderByOccurredAtDesc(pageable);
        }

        @Test
        @DisplayName("알림이 없으면 빈 페이지 반환")
        void findAllEmpty() {
            Pageable pageable = PageRequest.of(0, 20);
            given(notificationRepository.findAllByOrderByOccurredAtDesc(pageable))
                    .willReturn(Page.empty());

            Page<Notification> result = notificationService.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("안읽은 알림 수")
    class CountUnread {

        @Test
        @DisplayName("안읽은 알림 수를 반환한다")
        void countUnread() {
            given(notificationRepository.countByIsReadFalse()).willReturn(3L);

            long result = notificationService.countUnread();

            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("안읽은 알림 없으면 0 반환")
        void countUnreadZero() {
            given(notificationRepository.countByIsReadFalse()).willReturn(0L);

            long result = notificationService.countUnread();

            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("정상적으로 읽음 처리")
        void markAsReadNormal() {
            given(notificationRepository.findById(1L))
                    .willReturn(Optional.of(unreadNotification));

            notificationService.markAsRead(1L);

            assertThat(unreadNotification.isRead()).isTrue();
        }

        @Test
        @DisplayName("이미 읽은 알림도 멱등 처리")
        void markAsReadIdempotent() {
            given(notificationRepository.findById(1L))
                    .willReturn(Optional.of(readNotification));  // 이미 읽음

            assertThatCode(() -> notificationService.markAsRead(1L))
                    .doesNotThrowAnyException();

            assertThat(readNotification.isRead()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID면 예외")
        void rejectUnknownId() {
            given(notificationRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("전체 읽음 처리")
    class MarkAllAsRead {

        @Test
        @DisplayName("안읽은 알림 모두 읽음 처리")
        void markAllAsRead() {
            given(notificationRepository.findByIsReadFalseOrderByOccurredAtDesc(
                    any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(unreadNotification)));

            notificationService.markAllAsRead();

            assertThat(unreadNotification.isRead()).isTrue();
        }

        @Test
        @DisplayName("안읽은 알림 없어도 에러 없음 (멱등)")
        void markAllAsReadEmpty() {
            given(notificationRepository.findByIsReadFalseOrderByOccurredAtDesc(
                    any(Pageable.class)))
                    .willReturn(Page.empty());

            assertThatCode(() -> notificationService.markAllAsRead())
                    .doesNotThrowAnyException();
        }
    }
}
