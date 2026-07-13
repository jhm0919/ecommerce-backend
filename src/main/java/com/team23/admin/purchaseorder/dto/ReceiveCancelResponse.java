package com.team23.admin.purchaseorder.dto;

import com.team23.admin.purchaseorder.domain.PurchaseOrderStatus;

public record ReceiveCancelResponse(
        Long receiveHistoryId,
        Long skuId,
        int cancelledQuantity,
        int currentStock,
        PurchaseOrderStatus purchaseOrderStatus
) {
}
