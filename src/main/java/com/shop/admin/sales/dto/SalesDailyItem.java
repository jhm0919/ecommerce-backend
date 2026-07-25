package com.shop.admin.sales.dto;


public record SalesDailyItem(
        String productName,
        int price,
        int quantity,
        long amount
) {
}
