package com.shop.admin.purchaseorder.dto;

import com.shop.admin.purchaseorder.domain.PurchaseOrderStatus;

public record ReceiveCancelResponse(
        Long receiveHistoryId,
        Long skuId,
        int cancelledQuantity,
        int currentStock,
        PurchaseOrderStatus purchaseOrderStatus
) {
}
