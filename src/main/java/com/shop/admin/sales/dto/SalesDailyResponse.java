package com.shop.admin.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesDailyResponse(
        LocalDate date,
        List<SalesDailyItem> items,
        BigDecimal totalAmount,
        long totalQuantity
) {
}
