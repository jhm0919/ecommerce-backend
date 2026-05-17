package com.team23.customer.stock.dto;

import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;

public record ReceiveStockResponse(
        Long purchaseOrderId,
        Long skuId,
        int receivedQuantity,
        int currentStock,
        PurchaseOrderStatus status
) {
}
