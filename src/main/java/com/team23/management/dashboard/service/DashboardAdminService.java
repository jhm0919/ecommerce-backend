package com.team23.management.dashboard.service;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.management.dashboard.domain.AggregationUnit;
import com.team23.management.dashboard.dto.SalesTimeSeriesItem;
import com.team23.management.dashboard.dto.SalesTimeSeriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardAdminService {
    private static final long MAX_DAYS = 365L;

    private final OrderRepository orderRepository;

    public SalesTimeSeriesResponse getSalesTimeSeries(
            LocalDate startDate,
            LocalDate endDate,
            AggregationUnit unit
    ) {
        validateDateRange(startDate, endDate);

        // 날짜 범위 변환 (00:00:00 ~ 23:59:59)
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(23, 59, 59);

        // 집계 단위별 쿼리 호출
        List<Object[]> rawResults = (unit == AggregationUnit.DAILY)
                ? orderRepository.findDailySales(from, to)
                : orderRepository.findMonthlySales(from, to);

        // Object[] → DTO 변환
        List<SalesTimeSeriesItem> items = rawResults.stream()
                .map(row -> toItem(row, unit))
                .toList();

        // 합계 계산
        BigDecimal totalRevenue = items.stream()
                .map(SalesTimeSeriesItem::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrderCount = items.stream()
                .mapToLong(SalesTimeSeriesItem::orderCount)
                .sum();

        return new SalesTimeSeriesResponse(
                unit, startDate, endDate, items, totalRevenue, totalOrderCount
        );
    }

    private SalesTimeSeriesItem toItem(Object[] row, AggregationUnit unit) {
        String dateStr;
        if (unit == AggregationUnit.DAILY) {
            // FUNCTION('DATE', ...) 는 java.sql.Date 반환
            Date sqlDate = (Date) row[0];
            dateStr = sqlDate.toLocalDate().toString();   // "2026-01-15"
        } else {
            // FUNCTION('DATE_FORMAT', ...) 는 String 반환
            dateStr = (String) row[0];                     // "2026-01"
        }

        BigDecimal revenue = (BigDecimal) row[1];
        long count = ((Number) row[2]).longValue();

        return new SalesTimeSeriesItem(dateStr, revenue, count);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE) {};
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_DAYS) {
            throw new BusinessException(ErrorCode.DATE_RANGE_TOO_LONG) {};
        }
    }
}
