package com.team23.admin.dashboard.dto;

import com.team23.admin.dashboard.domain.AggregationUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesTimeSeriesResponse(
        AggregationUnit unit,
        LocalDate startDate,
        LocalDate endDate,
        List<SalesTimeSeriesItem> items,
        BigDecimal totalRevenue,
        long totalOrderCount
) {
}
