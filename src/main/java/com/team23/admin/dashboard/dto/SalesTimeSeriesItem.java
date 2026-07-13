package com.team23.admin.dashboard.dto;

import java.math.BigDecimal;

/**
 * 시계열 한 시점의 매출 데이터.
 *
 * <p>DAILY: date = "2026-01-15"
 * <p>MONTHLY: date = "2026-01"
 */
public record SalesTimeSeriesItem(
        String date,
        BigDecimal revenue,
        long orderCount
) {
}
