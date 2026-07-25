package com.shop.admin.sales.service;

import com.shop.admin.sales.dto.*;
import com.shop.admin.sales.exception.InvalidDateException;
import com.shop.admin.sales.exception.InvalidMonthException;
import com.shop.order.domain.OrderItem;
import com.shop.order.domain.OrderStatus;
import com.shop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesService {
    private final OrderRepository orderRepository;
    private static final List<OrderStatus> SALES_STATUSES =
            List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    @Transactional(readOnly = true)
    public SalesDailyResponse getDailySales(LocalDate date, Sort.Direction sort) {
        validateDate(date);

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime toExclusive = date.plusDays(1).atStartOfDay();

        List<OrderItem> orderItems = orderRepository.findDailySalesItems(from, toExclusive, SALES_STATUSES);

        List<SalesDailyItem> items = orderItems.stream()
                .map(item -> new SalesDailyItem( // SalesDailyItem 생성
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity(),
                        Math.multiplyExact((long) item.getPrice(), item.getQuantity()) // amount 계산
                )).toList();

        List<SalesDailyItem> sortedItems = getSortedItems(sort, items);

        long totalAmount = getDailyTotalAmount(items);

        long totalQuantity = items.stream()
                .mapToLong(SalesDailyItem::quantity)
                .sum();

        return new SalesDailyResponse(date, sortedItems, totalAmount, totalQuantity);
    }

    private static void validateDate(LocalDate date) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new InvalidDateException();
        }
    }

    private static @NonNull long getDailyTotalAmount(List<SalesDailyItem> items) {
        return items.stream()
                .map(SalesDailyItem::amount)
                .reduce(0L, Long::sum);
    }

    private static @NonNull List<SalesDailyItem> getSortedItems(Sort.Direction sort, List<SalesDailyItem> items) {
        return sort == Sort.Direction.DESC
                ? items.stream()
                    .sorted(Comparator.comparing(SalesDailyItem::amount).reversed())
                    .toList()
                : items.stream()
                    .sorted(Comparator.comparing(SalesDailyItem::amount))
                    .toList();
    }

    @Transactional(readOnly = true)
    public SalesMonthlyResponse getMonthlySales(YearMonth month) {
        validateMonth(month);

        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();
        LocalDateTime from = firstDay.atStartOfDay();
        LocalDateTime toExclusive = lastDay.plusDays(1).atStartOfDay();

        List<Object[]> rows = orderRepository.findMonthlySalesItems(from, toExclusive, SALES_STATUSES);
        List<SalesMonthlyItem> rawItems = rows.stream()
                .map(row -> {
                    LocalDate date = convertToLocalDate(row[0]);
                    BigDecimal amount = convertToBigDecimal(row[1]);

                    return new SalesMonthlyItem(date, amount);
                })
                .toList();

        // 월 내 날짜가 빠지지 않게 0원으로 채워 넣음
        List<SalesMonthlyItem> items = normalizeItems(rawItems, firstDay, lastDay);

        BigDecimal totalAmount = getMonthlyTotalAmount(rawItems);

        return new SalesMonthlyResponse(month, items, totalAmount);
    }

    private static void validateMonth(YearMonth month) {
        if (month == null || month.isAfter(YearMonth.now())) {
            throw new InvalidMonthException();
        }
    }

    private static LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }

        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }

        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }

        throw new IllegalArgumentException(
                "지원하지 않는 날짜 타입: " + value
        );
    }

    private static BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof BigDecimal amount) {
            return amount;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        throw new IllegalArgumentException(
                "지원하지 않는 금액 타입: " + value
        );
    }


    private static @NonNull List<SalesMonthlyItem> normalizeItems(List<SalesMonthlyItem> rawItems, LocalDate firstDay, LocalDate lastDay) {
        Map<LocalDate, BigDecimal> amountByDate = rawItems.stream()
                .collect(Collectors.toMap(
                        SalesMonthlyItem::date,
                        SalesMonthlyItem::dailyAmount,
                        BigDecimal::add
                ));

        return firstDay.datesUntil(lastDay.plusDays(1))
                .map(date -> new SalesMonthlyItem(
                        date,
                        amountByDate.getOrDefault(date, BigDecimal.ZERO)
                ))
                .toList();
    }

    private static @NonNull BigDecimal getMonthlyTotalAmount(List<SalesMonthlyItem> items) {
        return items.stream()
                .map(SalesMonthlyItem::dailyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
