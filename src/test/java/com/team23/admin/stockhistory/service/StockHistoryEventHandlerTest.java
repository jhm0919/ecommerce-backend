package com.team23.admin.stockhistory.service;

import com.team23.product.domain.StockChangedEvent;
import com.team23.admin.stockhistory.domain.StockChangeType;
import com.team23.admin.stockhistory.domain.StockHistory;
import com.team23.admin.stockhistory.domain.StockHistoryRecordedEvent;
import com.team23.admin.stockhistory.repository.StockHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockHistoryEventHandlerTest {

    @Mock private StockHistoryRepository stockHistoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private StockHistoryEventHandler stockHistoryEventHandler;

    @Nested
    @DisplayName("재고 변동 이벤트 처리 (handleStockChanged)")
    class HandleStockChanged {

        @Test
        @DisplayName("주문 재고 차감 이벤트 → 이력 저장")
        void saveOrderHistory() {
            StockChangedEvent event = new StockChangedEvent(
                    1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                    StockChangeType.ORDER, 3, 50, 47, 1001L
            );
            given(stockHistoryRepository.save(any(StockHistory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            stockHistoryEventHandler.handleStockChanged(event);

            ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
            verify(stockHistoryRepository).save(captor.capture());

            StockHistory saved = captor.getValue();
            assertThat(saved.getProductId()).isEqualTo(1L);
            assertThat(saved.getSkuId()).isEqualTo(100L);
            assertThat(saved.getChangeType()).isEqualTo(StockChangeType.ORDER);
            assertThat(saved.getQuantity()).isEqualTo(3);
            assertThat(saved.getStockBefore()).isEqualTo(50);
            assertThat(saved.getStockAfter()).isEqualTo(47);
            assertThat(saved.getOrderId()).isEqualTo(1001L);

            ArgumentCaptor<StockHistoryRecordedEvent> eventCaptor =
                    ArgumentCaptor.forClass(StockHistoryRecordedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().stockHistory()).isSameAs(saved);
        }

        @Test
        @DisplayName("어드민 재고 증가 이벤트 → orderId null로 저장")
        void saveAdminIncreaseHistory() {
            StockChangedEvent event = new StockChangedEvent(
                    1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                    StockChangeType.ADMIN_INCREASE, 10, 40, 50, null
            );
            given(stockHistoryRepository.save(any(StockHistory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            stockHistoryEventHandler.handleStockChanged(event);

            ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
            verify(stockHistoryRepository).save(captor.capture());

            StockHistory saved = captor.getValue();
            assertThat(saved.getChangeType()).isEqualTo(StockChangeType.ADMIN_INCREASE);
            assertThat(saved.getOrderId()).isNull();  // 주문 없음
        }

        @Test
        @DisplayName("주문 취소 이벤트 → 복구 이력 저장")
        void saveOrderCancelHistory() {
            StockChangedEvent event = new StockChangedEvent(
                    1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                    StockChangeType.ORDER_CANCEL, 3, 47, 50, 1001L
            );
            given(stockHistoryRepository.save(any(StockHistory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            stockHistoryEventHandler.handleStockChanged(event);

            ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
            verify(stockHistoryRepository).save(captor.capture());

            StockHistory saved = captor.getValue();
            assertThat(saved.getChangeType()).isEqualTo(StockChangeType.ORDER_CANCEL);
            assertThat(saved.getStockBefore()).isEqualTo(47);
            assertThat(saved.getStockAfter()).isEqualTo(50);  // 복구됐으니 증가
            assertThat(saved.getOrderId()).isEqualTo(1001L);
        }

        @Test
        @DisplayName("SKU 생성 이벤트 → 초기 재고 이력 저장")
        void saveSkuCreatedHistory() {
            StockChangedEvent event = new StockChangedEvent(
                    1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                    StockChangeType.SKU_CREATED, 30, 0, 30, null
            );
            given(stockHistoryRepository.save(any(StockHistory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            stockHistoryEventHandler.handleStockChanged(event);

            ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
            verify(stockHistoryRepository).save(captor.capture());

            StockHistory saved = captor.getValue();
            assertThat(saved.getChangeType()).isEqualTo(StockChangeType.SKU_CREATED);
            assertThat(saved.getStockBefore()).isEqualTo(0);   // 신규 생성
            assertThat(saved.getStockAfter()).isEqualTo(30);
            assertThat(saved.getOrderId()).isNull();
        }
    }
}
