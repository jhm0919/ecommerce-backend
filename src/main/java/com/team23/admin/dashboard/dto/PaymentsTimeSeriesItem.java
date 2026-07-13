package com.team23.admin.dashboard.dto;

import java.math.BigDecimal;

public record PaymentsTimeSeriesItem(
        String date,
        long paymentCount,
        BigDecimal totalAmount,
        BigDecimal averageAmount
) {
}
