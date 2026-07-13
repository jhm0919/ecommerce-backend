package com.team23.admin.stock.dto;

import com.team23.admin.purchaseorder.domain.PurchaseOrderStatus;

public record ReceiveStockResponse(
        Long purchaseOrderId,
        Long skuId,
        int receivedQuantity,
        int currentStock,
        PurchaseOrderStatus status
) {
}
