package com.team23.admin.stock.dto;

import com.team23.admin.stock.domain.ReceiveHistory;

import java.time.LocalDateTime;

public record ReceiveHistoryResponse(
        Long stockHistoryId,
        Long skuId,
        Long purchaseOrderId,
        int receivedQuantity,
        int stockAfter,
        LocalDateTime createdAt
) {
    public static ReceiveHistoryResponse from(ReceiveHistory sh) {
        return new ReceiveHistoryResponse(
                sh.getId(),
                sh.getSkuId(),
                sh.getPurchaseOrderId(),
                sh.getReceivedQuantity(),
                sh.getStockAfter(),
                sh.getCreatedAt()
        );
    }
}
