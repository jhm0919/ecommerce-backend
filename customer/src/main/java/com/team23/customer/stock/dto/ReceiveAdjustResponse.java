package com.team23.customer.stock.dto;

public record ReceiveAdjustResponse(
        Long receiveHistoryId,
        Long skuId,
        int originalQuantity,
        int adjustedQuantity,
        int currentStock,
        String reason
) {
}
