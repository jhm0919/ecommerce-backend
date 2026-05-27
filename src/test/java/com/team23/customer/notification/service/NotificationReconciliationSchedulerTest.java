package com.team23.customer.notification.service;

import com.team23.customer.stockhistory.domain.StockChangeType;
import com.team23.customer.stockhistory.domain.StockHistory;
import com.team23.customer.stockhistory.repository.StockHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationReconciliationSchedulerTest {

    @Mock private StockHistoryRepository stockHistoryRepository;
    @Mock private NotificationCreatorService notificationCreatorService;

    @InjectMocks private NotificationReconciliationScheduler scheduler;

    @Test
    @DisplayName("최근 품절 전이 이력을 모든 페이지에서 조회해 알림 생성 서비스에 위임")
    void reconcileSoldOutNotifications() {
        StockHistory first = history(1L);
        StockHistory second = history(2L);
        Page<StockHistory> firstPage = new PageImpl<>(
                List.of(first),
                PageRequest.of(0, 1),
                2
        );
        Page<StockHistory> secondPage = new PageImpl<>(
                List.of(second),
                PageRequest.of(1, 1),
                2
        );
        given(stockHistoryRepository.findSoldOutTransitionsSince(
                any(), any(), any(Pageable.class)))
                .willReturn(firstPage, secondPage);

        scheduler.reconcileSoldOutNotifications();

        verify(notificationCreatorService).createSoldOutNotificationIfNeeded(first);
        verify(notificationCreatorService).createSoldOutNotificationIfNeeded(second);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StockChangeType>> typesCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(stockHistoryRepository, times(2)).findSoldOutTransitionsSince(
                typesCaptor.capture(), any(), any(Pageable.class));

        assertThat(typesCaptor.getAllValues().get(0))
                .containsExactly(StockChangeType.ORDER, StockChangeType.ADMIN_DECREASE);
    }

    @Test
    @DisplayName("품절 전이 이력이 없으면 알림 생성 서비스를 호출하지 않는다")
    void skipWhenNoSoldOutTransitions() {
        given(stockHistoryRepository.findSoldOutTransitionsSince(
                any(), any(), any(Pageable.class)))
                .willReturn(Page.empty(PageRequest.of(0, 500)));

        scheduler.reconcileSoldOutNotifications();

        verify(notificationCreatorService, never())
                .createSoldOutNotificationIfNeeded(any(StockHistory.class));
        verify(stockHistoryRepository).findSoldOutTransitionsSince(
                any(), any(), any(Pageable.class));
    }

    private StockHistory history(Long orderId) {
        return StockHistory.of(
                10L, "티셔츠", 100L, "SKU-10-001", "색상=검정",
                StockChangeType.ORDER, 3, 3, 0, orderId
        );
    }
}
