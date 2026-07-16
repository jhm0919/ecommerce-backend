package com.shop.admin.sales.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record SalesMonthlyResponse(
        YearMonth month,
        List<SalesMonthlyItem> items,
        BigDecimal totalAmount
) {
}
