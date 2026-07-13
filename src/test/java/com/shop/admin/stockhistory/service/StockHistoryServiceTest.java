package com.shop.admin.stockhistory.service;

import com.shop.admin.stockhistory.domain.StockChangeType;
import com.shop.admin.stockhistory.domain.StockHistory;
import com.shop.admin.stockhistory.repository.StockHistoryRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockHistoryServiceTest {

    @Mock private StockHistoryRepository stockHistoryRepository;

    @InjectMocks private StockHistoryService stockHistoryService;

    private StockHistory orderHistory;
    private StockHistory adminHistory;

    @BeforeEach
    void setUp() {
        orderHistory = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockChangeType.ORDER, 3, 50, 47, 1001L
        );
        adminHistory = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockChangeType.ADMIN_INCREASE, 10, 47, 57, null
        );
    }

    @Nested
    @DisplayName("재고 변동 이력 조회 (findHistories)")
    class FindHistories {

        @Test
        @DisplayName("Product 단위 전체 이력 조회")
        void findAllByProduct() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(null), eq(null), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory, adminHistory)));

            Page<StockHistory> result = stockHistoryService.findHistories(
                    1L, null, null, pageable);

            assertThat(result.getContent()).hasSize(2);
            verify(stockHistoryRepository).findHistories(1L, null, null, pageable);
        }

        @Test
        @DisplayName("SKU 필터 적용")
        void filterBySkuId() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(100L), eq(null), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory)));

            Page<StockHistory> result = stockHistoryService.findHistories(
                    1L, 100L, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(stockHistoryRepository).findHistories(1L, 100L, null, pageable);
        }

        @Test
        @DisplayName("changeType 필터 적용")
        void filterByChangeType() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(null), eq(StockChangeType.ORDER), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory)));

            Page<StockHistory> result = stockHistoryService.findHistories(
                    1L, null, StockChangeType.ORDER, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getChangeType())
                    .isEqualTo(StockChangeType.ORDER);
        }

        @Test
        @DisplayName("SKU + changeType 조합 필터")
        void filterBySkuAndChangeType() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(100L), eq(StockChangeType.ORDER), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory)));

            Page<StockHistory> result = stockHistoryService.findHistories(
                    1L, 100L, StockChangeType.ORDER, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("이력이 없으면 빈 페이지 반환")
        void emptyResult() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(null), eq(null), eq(pageable)))
                    .willReturn(Page.empty());

            Page<StockHistory> result = stockHistoryService.findHistories(
                    1L, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}