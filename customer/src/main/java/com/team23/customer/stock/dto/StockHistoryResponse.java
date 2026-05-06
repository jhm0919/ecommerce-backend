package com.team23.customer.stock.dto;

import com.team23.customer.stock.domain.StockHistory;

import java.time.LocalDateTime;

public record StockHistoryResponse(
        Long stockHistoryId,
        Long skuId,
        Long purchaseOrderId,
        int receivedQuantity,
        int stockAfter,
        LocalDateTime createdAt
) {
    public static StockHistoryResponse from(StockHistory sh) {
        return new StockHistoryResponse(
                sh.getId(),
                sh.getSkuId(),
                sh.getPurchaseOrderId(),
                sh.getReceivedQuantity(),
                sh.getStockAfter(),
                sh.getCreatedAt()
        );
    }
}
