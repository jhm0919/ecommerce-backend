package com.shop.admin.sales.dto;

import java.time.LocalDate;
import java.util.List;

public record SalesDailyResponse(
        LocalDate date,
        List<SalesDailyItem> items,
        long totalAmount,
        long totalQuantity
) {
}
