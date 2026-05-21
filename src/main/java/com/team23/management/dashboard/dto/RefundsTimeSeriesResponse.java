package com.team23.management.dashboard.dto;

import com.team23.management.dashboard.domain.AggregationUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RefundsTimeSeriesResponse(
        AggregationUnit unit,
        LocalDate startDate,
        LocalDate endDate,
        List<RefundsTimeSeriesItem> items,
        long totalRefundCount,
        BigDecimal totalRefundAmount
) {
}
