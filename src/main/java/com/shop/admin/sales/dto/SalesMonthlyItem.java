package com.shop.admin.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesMonthlyItem(
        LocalDate date,
        BigDecimal dailyAmount
) {
}
