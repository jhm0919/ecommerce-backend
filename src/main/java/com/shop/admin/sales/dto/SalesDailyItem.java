package com.shop.admin.sales.dto;

import java.math.BigDecimal;

public record SalesDailyItem(
        String productName,
        BigDecimal price,
        int quantity,
        BigDecimal amount
) {
}
