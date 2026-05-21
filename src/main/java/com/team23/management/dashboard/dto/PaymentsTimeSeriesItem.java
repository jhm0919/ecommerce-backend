package com.team23.management.dashboard.dto;

import java.math.BigDecimal;

public record PaymentsTimeSeriesItem(
        String date,
        long paymentCount,
        BigDecimal totalAmount,
        BigDecimal averageAmount
) {
}
