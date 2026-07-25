package com.shop.admin.sales.service;

import com.shop.admin.sales.dto.SalesDailyItem;
import com.shop.admin.sales.dto.SalesDailyResponse;
import com.shop.admin.sales.dto.SalesMonthlyItem;
import com.shop.admin.sales.dto.SalesMonthlyResponse;
import com.shop.admin.sales.exception.InvalidDateException;
import com.shop.admin.sales.exception.InvalidMonthException;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.order.domain.OrderItem;
import com.shop.order.domain.OrderStatus;
import com.shop.order.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SalesService salesService;

    private OrderItem orderItem(String productName, int price, int quantity) {
        OrderItem item = mock(OrderItem.class);
        given(item.getProductName()).willReturn(productName);
        given(item.getPrice()).willReturn(price);
        given(item.getQuantity()).willReturn(quantity);
        return item;
    }

    @Test
    @DisplayName("일별 매출 계산")
    void dailySalesCalculatesTotals() {
        LocalDate date = LocalDate.of(2026, 7, 16);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime toExclusive = date.plusDays(1).atStartOfDay();

        List<OrderItem> items = List.of(
                orderItem("A상품", 10000, 2),
                orderItem("B상품", 15000, 1)
        );

        given(orderRepository.findDailySalesItems(
                from, toExclusive, List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED)
        )).willReturn(items);

        SalesDailyResponse result = salesService.getDailySales(date, Sort.Direction.ASC);

        assertThat(result.totalAmount()).isEqualByComparingTo(35000L);
        assertThat(result.totalQuantity()).isEqualTo(3);
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("일별 매출 오름차순 정렬")
    void getDailySales_sortsByAmountAscending() {
        LocalDate date = LocalDate.of(2026, 7, 16);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime toExclusive = date.plusDays(1).atStartOfDay();

        List<OrderItem> items = List.of(
                orderItem("B상품", 30000, 1),
                orderItem("A상품", 10000, 2),
                orderItem("C상품", 20000, 4)
        );

        given(orderRepository.findDailySalesItems(
                eq(from),
                eq(toExclusive),
                eq(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED))
        )).willReturn(items);

        SalesDailyResponse result = salesService.getDailySales(date, Sort.Direction.ASC);

        assertThat(result.items())
                .extracting(SalesDailyItem::amount)
                .containsExactly(20000L, 30000L, 80000L);
    }

    @Test
    @DisplayName("일별 매출 내림차순 정렬")
    void getDailySales_sortsByAmountDescending() {
        LocalDate date = LocalDate.of(2026, 7, 16);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime toExclusive = date.plusDays(1).atStartOfDay();

        List<OrderItem> items = List.of(
                orderItem("B상품", 30000, 1),
                orderItem("A상품", 10000, 2),
                orderItem("C상품", 20000, 4)
        );

        given(orderRepository.findDailySalesItems(
                eq(from),
                eq(toExclusive),
                eq(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED))
        )).willReturn(items);

        SalesDailyResponse result = salesService.getDailySales(date, Sort.Direction.DESC);

        assertThat(result.items())
                .extracting(SalesDailyItem::amount)
                .containsExactly(80000L, 30000L, 20000L);
    }

    @Test
    @DisplayName("매출이 없는 날은 0원으로 채움")
    void monthlySales_fillsMissingDaysWithZero() {
        YearMonth month = YearMonth.of(2026, 7);
        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();
        LocalDateTime from = firstDay.atStartOfDay();
        LocalDateTime toExclusive = lastDay.plusDays(1).atStartOfDay();

        given(orderRepository.findMonthlySalesItems(
                eq(from),
                eq(toExclusive),
                eq(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED))
        )).willReturn(List.<Object[]>of(
                new Object[]{LocalDate.of(2026, 7, 1), BigDecimal.valueOf(10000)},
                new Object[]{LocalDate.of(2026, 7, 3), BigDecimal.valueOf(20000)}
        ));


        SalesMonthlyResponse result = salesService.getMonthlySales(month);

        assertThat(result.items()).hasSize(31);
        assertThat(result.items().get(0))
                .extracting(SalesMonthlyItem::date, SalesMonthlyItem::dailyAmount)
                .containsExactly(LocalDate.of(2026, 7, 1), BigDecimal.valueOf(10000));

        assertThat(result.items().get(1))
                .extracting(SalesMonthlyItem::date, SalesMonthlyItem::dailyAmount)
                .containsExactly(LocalDate.of(2026, 7, 2), BigDecimal.ZERO);

        assertThat(result.items().get(2))
                .extracting(SalesMonthlyItem::date, SalesMonthlyItem::dailyAmount)
                .containsExactly(LocalDate.of(2026, 7, 3), BigDecimal.valueOf(20000));
        assertThat(result.totalAmount()).isEqualByComparingTo("30000");
    }

    @Test
    @DisplayName("유효하지 않는 날짜 → 예외")
    void invalidDate_throwsException() {
        assertThatThrownBy(() -> salesService.getDailySales(null, Sort.Direction.ASC))
                .isInstanceOf(InvalidDateException.class);

        assertThatThrownBy(() -> salesService.getDailySales(LocalDate.now().plusDays(1), Sort.Direction.ASC))
                .isInstanceOf(InvalidDateException.class);
    }

    @Test
    @DisplayName("유효하지 않는 달 → 예외")
    void invalidMonth_throwsException() {
        assertThatThrownBy(() -> salesService.getMonthlySales(null))
                .isInstanceOf(InvalidMonthException.class);

        assertThatThrownBy(() -> salesService.getMonthlySales(YearMonth.now().plusMonths(1)))
                .isInstanceOf(InvalidMonthException.class);
    }
}