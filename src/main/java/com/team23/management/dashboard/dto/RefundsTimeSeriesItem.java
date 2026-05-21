package com.team23.management.dashboard.dto;

import java.math.BigDecimal;

public record RefundsTimeSeriesItem(
        String date,
        long refundCount,
        BigDecimal refundAmount
) {
}
