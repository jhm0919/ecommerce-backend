package com.team23.customer.stockhistory.dto;

import com.team23.customer.stockhistory.domain.StockChangeType;
import com.team23.customer.stockhistory.domain.StockHistory;

import java.time.LocalDateTime;

public record StockHistoryResponse(
        Long id,
        Long productId,
        String productName,
        Long skuId,
        String skuCode,
        String skuOptionsSnapshot,
        StockChangeType changeType,
        int quantity,
        int stockBefore,
        int stockAfter,
        Long orderId,
        LocalDateTime occurredAt
) {
    public static StockHistoryResponse from(StockHistory history) {
        return new StockHistoryResponse(
                history.getId(),
                history.getProductId(),
                history.getProductName(),
                history.getSkuId(),
                history.getSkuCode(),
                history.getSkuOptionsSnapshot(),
                history.getChangeType(),
                history.getQuantity(),
                history.getStockBefore(),
                history.getStockAfter(),
                history.getOrderId(),
                history.getOccurredAt()
        );
    }
}