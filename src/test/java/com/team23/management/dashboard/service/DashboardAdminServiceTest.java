package com.team23.management.dashboard.service;

import com.team23.common.exception.BusinessException;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.customer.product.domain.Money;
import com.team23.management.dashboard.domain.AggregationUnit;
import com.team23.management.dashboard.dto.PaymentsTimeSeriesResponse;
import com.team23.management.dashboard.dto.RefundsTimeSeriesResponse;
import com.team23.management.dashboard.dto.SalesTimeSeriesResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DashboardAdminServiceTest {
    @Autowired
    DashboardAdminService dashboardAdminService;
    @Autowired
    OrderRepository orderRepository;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
    }

    /**
     * Order 만 직접 native SQL 로 INSERT.
     * OrderItem 은 본 이슈의 집계와 무관하므로 우회.
     */
    private void insertOrder(BigDecimal amount, OrderStatus status, LocalDateTime createdAt) {
        em.createNativeQuery("""
                INSERT INTO orders 
                    (order_number, member_id, total_amount, total_currency, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """)
                .setParameter(1, "TEST-" + System.nanoTime())
                .setParameter(2, 1L)
                .setParameter(3, amount)
                .setParameter(4, "KRW")
                .setParameter(5, status.name())
                .setParameter(6, createdAt)
                .setParameter(7, createdAt)
                .executeUpdate();
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("DAILY 집계 - 날짜별 매출 합산 + 정렬")
    void getDailySales_aggregatesAndOrders() {
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 1, 15, 0));
        insertOrder(new BigDecimal("30000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 3, 12, 0));

        SalesTimeSeriesResponse response = dashboardAdminService.getSalesTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 5),
                AggregationUnit.DAILY
        );
        assertThat(response.items()).hasSize(2);   // 빈 날짜 자동 채우기 없음
        assertThat(response.items().get(0).date()).isEqualTo("2026-01-01");
        assertThat(response.items().get(0).revenue()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(response.items().get(0).orderCount()).isEqualTo(2);
        assertThat(response.items().get(1).date()).isEqualTo("2026-01-03");

        assertThat(response.totalRevenue()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(response.totalOrderCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("CANCELLED 주문은 매출 집계에서 제외")
    void cancelledOrder_excluded() {
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        insertOrder(new BigDecimal("50000"), OrderStatus.CANCELLED,
                LocalDateTime.of(2026, 1, 1, 11, 0));

        SalesTimeSeriesResponse response = dashboardAdminService.getSalesTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),
                AggregationUnit.DAILY
        );

        assertThat(response.totalRevenue()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(response.totalOrderCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("데이터 없는 기간 - 빈 목록")
    void noData_returnsEmpty() {
        SalesTimeSeriesResponse response = dashboardAdminService.getSalesTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                AggregationUnit.DAILY
        );

        assertThat(response.items()).isEmpty();
        assertThat(response.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.totalOrderCount()).isZero();
    }

    @Test
    @DisplayName("startDate > endDate → 예외")
    void invalidDateRange_throws() {
        assertThatThrownBy(() ->
                dashboardAdminService.getSalesTimeSeries(
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 5),
                        AggregationUnit.DAILY
                )
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("기간 365일 초과 → 예외")
    void rangeTooLong_throws() {
        assertThatThrownBy(() ->
                dashboardAdminService.getSalesTimeSeries(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2027, 6, 1),
                        AggregationUnit.DAILY
                )
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("MONTHLY 집계 - 월별 매출 합산")
    void monthlySales_aggregates() {
        insertOrder(new BigDecimal("100000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 15, 10, 0));
        insertOrder(new BigDecimal("50000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 2, 10, 10, 0));
        insertOrder(new BigDecimal("30000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 2, 20, 10, 0));

        SalesTimeSeriesResponse response = dashboardAdminService.getSalesTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                AggregationUnit.MONTHLY
        );

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).date()).isEqualTo("2026-01");
        assertThat(response.items().get(0).revenue()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(response.items().get(1).date()).isEqualTo("2026-02");
        assertThat(response.items().get(1).revenue()).isEqualByComparingTo(new BigDecimal("80000"));
    }

    @Test
    @DisplayName("endDate 의 23:59:59.999 에 생성된 주문도 집계에 포함")
    void boundaryTime_isIncluded() {
        // 2026-01-01 23:59:59.999 (거의 자정 직전)
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 1, 23, 59, 59, 999_000_000));

        SalesTimeSeriesResponse response = dashboardAdminService.getSalesTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),   // 같은 날
                AggregationUnit.DAILY
        );

        assertThat(response.totalOrderCount()).isEqualTo(1);
        assertThat(response.totalRevenue())
                .isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("다음 날 00:00:00 에 생성된 주문은 포함 안 됨 (배타적 경계)")
    void nextDayStart_isExcluded() {
        // 2026-01-02 00:00:00 (다음 날 정각)
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING,
                LocalDateTime.of(2026, 1, 2, 0, 0, 0));

        SalesTimeSeriesResponse response = dashboardAdminService.getSalesTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),   // 1월 1일만 조회
                AggregationUnit.DAILY
        );

        assertThat(response.totalOrderCount()).isZero();
        assertThat(response.items()).isEmpty();
    }

    // 결제 건수
    @Test
    @DisplayName("[payments] DAILY 집계 - 결제 건수/총액/평균")
    void getDailyPayments_aggregates() {
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 10, 0));
        insertOrder(new BigDecimal("20000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 11, 0));
        insertOrder(new BigDecimal("30000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 12, 0));

        PaymentsTimeSeriesResponse response = dashboardAdminService.getPaymentsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),
                AggregationUnit.DAILY
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).paymentCount()).isEqualTo(3);
        assertThat(response.items().get(0).totalAmount()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(response.items().get(0).averageAmount()).isEqualByComparingTo(new BigDecimal("20000"));   // 60000 / 3

        assertThat(response.totalPaymentCount()).isEqualTo(3);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(response.averageAmount()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    @DisplayName("[payments] CANCELLED 도 포함 (sales 와 차이점)")
    void cancelledOrder_isIncluded() {
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 10, 0));
        insertOrder(new BigDecimal("50000"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 1, 1, 11, 0));   // 포함됨!

        PaymentsTimeSeriesResponse response = dashboardAdminService.getPaymentsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),
                AggregationUnit.DAILY
        );

        assertThat(response.totalPaymentCount()).isEqualTo(2);   // 둘 다 포함
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("60000"));   // 10000 + 50000
    }

    @Test
    @DisplayName("[payments] 평균 - 무한소수는 반올림 (HALF_UP)")
    void averageRounding_halfUp() {
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 10, 0));
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 11, 0));
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 12, 0));
        // 평균: 30000 / 3 = 10000 (정확)

        PaymentsTimeSeriesResponse response = dashboardAdminService.getPaymentsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),
                AggregationUnit.DAILY
        );

        assertThat(response.averageAmount()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("[payments] 데이터 없는 기간 - 평균 0")
    void noData_averageZero() {
        PaymentsTimeSeriesResponse response = dashboardAdminService.getPaymentsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                AggregationUnit.DAILY
        );

        assertThat(response.items()).isEmpty();
        assertThat(response.totalPaymentCount()).isZero();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.averageAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("[payments] MONTHLY 집계")
    void monthlyPayments_aggregates() {
        insertOrder(new BigDecimal("100000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 15, 10, 0));
        insertOrder(new BigDecimal("50000"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 2, 10, 10, 0));
        insertOrder(new BigDecimal("30000"), OrderStatus.PENDING, LocalDateTime.of(2026, 2, 20, 10, 0));

        PaymentsTimeSeriesResponse response = dashboardAdminService.getPaymentsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                AggregationUnit.MONTHLY
        );

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).date()).isEqualTo("2026-01");
        assertThat(response.items().get(0).totalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(response.items().get(1).date()).isEqualTo("2026-02");
        assertThat(response.items().get(1).totalAmount()).isEqualByComparingTo(new BigDecimal("80000"));   // CANCELLED 도 포함
    }

    @Test
    @DisplayName("[payments] startDate > endDate → 예외")
    void payments_invalidDateRange_throws() {
        assertThatThrownBy(() ->
                dashboardAdminService.getPaymentsTimeSeries(
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 5),
                        AggregationUnit.DAILY
                )
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("[refunds] DAILY 집계 - CANCELLED 만 포함")
    void getDailyRefunds_onlyCancelled() {
        insertOrder(new BigDecimal("10000"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 1, 1, 10, 0));
        insertOrder(new BigDecimal("20000"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 1, 1, 11, 0));
        insertOrder(new BigDecimal("50000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 12, 0));   // 제외 대상
        insertOrder(new BigDecimal("70000"), OrderStatus.CONFIRMED, LocalDateTime.of(2026, 1, 1, 13, 0));   // 제외 대상

        RefundsTimeSeriesResponse response = dashboardAdminService.getRefundsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),
                AggregationUnit.DAILY
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).refundCount()).isEqualTo(2);
        assertThat(response.items().get(0).refundAmount()).isEqualByComparingTo(new BigDecimal("30000"));

        assertThat(response.totalRefundCount()).isEqualTo(2);
        assertThat(response.totalRefundAmount()).isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("[refunds] 환불 없는 기간 - 빈 목록")
    void noRefunds_returnsEmpty() {
        insertOrder(new BigDecimal("10000"), OrderStatus.PENDING, LocalDateTime.of(2026, 1, 1, 10, 0));   // 취소 안 됨

        RefundsTimeSeriesResponse response = dashboardAdminService.getRefundsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1),
                AggregationUnit.DAILY
        );

        assertThat(response.items()).isEmpty();
        assertThat(response.totalRefundCount()).isZero();
        assertThat(response.totalRefundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("[refunds] MONTHLY 집계 - CANCELLED 만")
    void monthlyRefunds_aggregates() {
        insertOrder(new BigDecimal("100000"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 1, 15, 10, 0));
        insertOrder(new BigDecimal("50000"), OrderStatus.PENDING, LocalDateTime.of(2026, 2, 10, 10, 0));   // 제외
        insertOrder(new BigDecimal("30000"), OrderStatus.CANCELLED, LocalDateTime.of(2026, 2, 20, 10, 0));

        RefundsTimeSeriesResponse response = dashboardAdminService.getRefundsTimeSeries(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                AggregationUnit.MONTHLY
        );

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).date()).isEqualTo("2026-01");
        assertThat(response.items().get(0).refundAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(response.items().get(1).date()).isEqualTo("2026-02");
        assertThat(response.items().get(1).refundAmount()).isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("[refunds] startDate > endDate → 예외")
    void refunds_invalidDateRange_throws() {
        assertThatThrownBy(() ->
                dashboardAdminService.getRefundsTimeSeries(
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 5),
                        AggregationUnit.DAILY
                )
        ).isInstanceOf(BusinessException.class);
    }

}