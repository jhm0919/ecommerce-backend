package com.team23.admin.dashboard.service;

import com.team23.admin.dashboard.dto.*;
import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;
import com.team23.order.repository.OrderRepository;
import com.team23.admin.dashboard.domain.AggregationUnit;
import com.team23.admin.dashboard.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        LocalDateTime toExclusive = endDate.plusDays(1).atStartOfDay();

        // 집계 단위별 쿼리 호출
        List<Object[]> rawResults = (unit == AggregationUnit.DAILY)
                ? orderRepository.findDailySales(from, toExclusive)
                : orderRepository.findMonthlySales(from, toExclusive);

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
        BigDecimal revenue;
        long count;

        if (unit == AggregationUnit.DAILY) {
            // [date(sql.Date), revenue, count]
            Date sqlDate = (Date) row[0];
            dateStr = sqlDate.toLocalDate().toString();   // "2026-01-15"
            revenue = (BigDecimal) row[1];
            count = ((Number) row[2]).longValue();
        } else {
            // [year, month, revenue, count]
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            dateStr = String.format("%04d-%02d", year, month);   // "2026-01"
            revenue = (BigDecimal) row[2];
            count = ((Number) row[3]).longValue();
        }

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

    public PaymentsTimeSeriesResponse getPaymentsTimeSeries(LocalDate startDate, LocalDate endDate, AggregationUnit unit) {
        validateDateRange(startDate, endDate);

        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime toExclusive = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rawResults = (unit == AggregationUnit.DAILY)
                ? orderRepository.findDailyPayments(from, toExclusive)
                : orderRepository.findMonthlyPayments(from, toExclusive);

        List<PaymentsTimeSeriesItem> items = rawResults.stream()
                .map(row -> toPaymentsItem(row, unit))
                .toList();

        // 합계
        long totalPaymentCount = items.stream()
                .mapToLong(PaymentsTimeSeriesItem::paymentCount)
                .sum();
        BigDecimal totalAmount = items.stream()
                .map(PaymentsTimeSeriesItem::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = calculateAverage(totalAmount, totalPaymentCount);

        return new PaymentsTimeSeriesResponse(
                unit, startDate, endDate, items,
                totalPaymentCount, totalAmount, averageAmount
        );
    }

    private PaymentsTimeSeriesItem toPaymentsItem(Object[] row, AggregationUnit unit) {
        String dateStr;
        BigDecimal totalAmount;
        long count;

        if (unit == AggregationUnit.DAILY) {
            // [date, totalAmount, count]
            Date sqlDate = (Date) row[0];
            dateStr = sqlDate.toLocalDate().toString();
            totalAmount = (BigDecimal) row[1];
            count = ((Number) row[2]).longValue();
        } else {
            // [year, month, totalAmount, count]
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            dateStr = String.format("%04d-%02d", year, month);
            totalAmount = (BigDecimal) row[2];
            count = ((Number) row[3]).longValue();
        }

        BigDecimal averageAmount = calculateAverage(totalAmount, count);
        return new PaymentsTimeSeriesItem(dateStr, count, totalAmount, averageAmount);
    }

    /**
     * 평균 금액 계산. count=0 일 때 0 반환 (division by zero 방지).
     * KRW 가정으로 소수점 0자리, HALF_UP 반올림.
     */
    private BigDecimal calculateAverage(BigDecimal total, long count) {
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP);
    }

    public RefundsTimeSeriesResponse getRefundsTimeSeries(LocalDate startDate, LocalDate endDate, AggregationUnit unit) {
        validateDateRange(startDate, endDate);

        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime toExclusive = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rawResults = (unit == AggregationUnit.DAILY)
                ? orderRepository.findDailyRefunds(from, toExclusive)
                : orderRepository.findMonthlyRefunds(from, toExclusive);

        List<RefundsTimeSeriesItem> items = rawResults.stream()
                .map(row -> toRefundsItem(row, unit))
                .toList();

        long totalRefundCount = items.stream()
                .mapToLong(RefundsTimeSeriesItem::refundCount)
                .sum();
        BigDecimal totalRefundAmount = items.stream()
                .map(RefundsTimeSeriesItem::refundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RefundsTimeSeriesResponse(unit, startDate, endDate, items, totalRefundCount, totalRefundAmount);
    }

    private RefundsTimeSeriesItem toRefundsItem(Object[] row, AggregationUnit unit) {
        String dateStr;
        BigDecimal refundAmount;
        long count;

        if (unit == AggregationUnit.DAILY) {
            Date sqlDate = (Date) row[0];
            dateStr = sqlDate.toLocalDate().toString();
            refundAmount = (BigDecimal) row[1];
            count = ((Number) row[2]).longValue();
        } else {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            dateStr = String.format("%04d-%02d", year, month);
            refundAmount = (BigDecimal) row[2];
            count = ((Number) row[3]).longValue();
        }

        return new RefundsTimeSeriesItem(dateStr, count, refundAmount);
    }
}
