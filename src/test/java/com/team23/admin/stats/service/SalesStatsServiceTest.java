package com.team23.admin.stats.service;

import com.team23.order.repository.OrderItemRepository;
import com.team23.order.repository.OrderRepository;
import com.team23.admin.stats.dto.SalesStatsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalesStatsServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks private SalesStatsService salesStatsService;

    private static final LocalDate FROM = LocalDate.of(2026, 5, 1);
    private static final LocalDate TO = LocalDate.of(2026, 5, 12);

    @Nested
    @DisplayName("판매 실적 통계 조회 (getSalesStats)")
    class GetSalesStats {

        @Test
        @DisplayName("정상 조회 — 매출액, 주문/취소 건수, 인기 상품 포함")
        void getSalesStatsNormal() {
            List<Object[]> revenueResult = List.<Object[]>of(
                    new Object[]{new BigDecimal("1250000"), 42L}
            );
            given(orderRepository.findRevenueAndOrderCount(any(), any()))
                    .willReturn(revenueResult);

            given(orderRepository.countCancelledOrders(any(), any()))
                    .willReturn(3L);

            List<Object[]> popularResult = List.<Object[]>of(
                    new Object[]{1L, "티셔츠", 150L, new BigDecimal("4485000")},
                    new Object[]{2L, "청바지", 87L, new BigDecimal("4341300")}
            );
            given(orderItemRepository.findTop5PopularProducts(any(), any()))
                    .willReturn(popularResult);

            SalesStatsResponse result = salesStatsService.getSalesStats(FROM, TO);

            assertThat(result.totalRevenue()).isEqualByComparingTo("1250000");
            assertThat(result.currency()).isEqualTo("KRW");
            assertThat(result.orderCount()).isEqualTo(42L);
            assertThat(result.cancelCount()).isEqualTo(3L);
            assertThat(result.popularProducts()).hasSize(2);
            assertThat(result.popularProducts().get(0).productName()).isEqualTo("티셔츠");
            assertThat(result.popularProducts().get(0).totalSoldQuantity()).isEqualTo(150L);
        }

        @Test
        @DisplayName("주문 없으면 매출 0, 건수 0")
        void noOrders() {
            List<Object[]> emptyRevenue = List.<Object[]>of(
                    new Object[]{null, 0L}
            );
            given(orderRepository.findRevenueAndOrderCount(any(), any()))
                    .willReturn(emptyRevenue);

            given(orderRepository.countCancelledOrders(any(), any()))
                    .willReturn(0L);

            List<Object[]> emptyPopular = List.<Object[]>of();
            given(orderItemRepository.findTop5PopularProducts(any(), any()))
                    .willReturn(emptyPopular);

            SalesStatsResponse result = salesStatsService.getSalesStats(FROM, TO);

            assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.orderCount()).isZero();
            assertThat(result.cancelCount()).isZero();
            assertThat(result.popularProducts()).isEmpty();
        }

        @Test
        @DisplayName("인기 상품 최대 5개")
        void popularProductsMaxFive() {
            List<Object[]> revenueResult = List.<Object[]>of(
                    new Object[]{new BigDecimal("500000"), 10L}
            );
            given(orderRepository.findRevenueAndOrderCount(any(), any()))
                    .willReturn(revenueResult);
            given(orderRepository.countCancelledOrders(any(), any()))
                    .willReturn(0L);

            List<Object[]> popularResult = List.<Object[]>of(
                    new Object[]{1L, "상품1", 100L, new BigDecimal("100000")},
                    new Object[]{2L, "상품2", 90L, new BigDecimal("90000")},
                    new Object[]{3L, "상품3", 80L, new BigDecimal("80000")},
                    new Object[]{4L, "상품4", 70L, new BigDecimal("70000")},
                    new Object[]{5L, "상품5", 60L, new BigDecimal("60000")}
            );
            given(orderItemRepository.findTop5PopularProducts(any(), any()))
                    .willReturn(popularResult);

            SalesStatsResponse result = salesStatsService.getSalesStats(FROM, TO);

            assertThat(result.popularProducts()).hasSize(5);
        }

        @Test
        @DisplayName("날짜 범위 변환 — from은 00:00, to는 23:59:59")
        void dateRangeConversion() {
            List<Object[]> revenueResult = List.<Object[]>of(
                    new Object[]{BigDecimal.ZERO, 0L}
            );
            given(orderRepository.findRevenueAndOrderCount(any(), any()))
                    .willReturn(revenueResult);
            given(orderRepository.countCancelledOrders(any(), any()))
                    .willReturn(0L);

            List<Object[]> emptyPopular = List.<Object[]>of();
            given(orderItemRepository.findTop5PopularProducts(any(), any()))
                    .willReturn(emptyPopular);

            salesStatsService.getSalesStats(FROM, TO);

            verify(orderRepository).findRevenueAndOrderCount(
                    LocalDateTime.of(2026, 5, 1, 0, 0, 0),
                    LocalDateTime.of(2026, 5, 12, 23, 59, 59)
            );
        }
    }
}