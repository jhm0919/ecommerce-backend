package com.shop.admin.stock.dto;

import com.shop.admin.purchaseorder.domain.PurchaseOrderStatus;

public record ReceiveStockResponse(
        Long purchaseOrderId,
        Long skuId,
        int receivedQuantity,
        int currentStock,
        PurchaseOrderStatus status
) {
}
