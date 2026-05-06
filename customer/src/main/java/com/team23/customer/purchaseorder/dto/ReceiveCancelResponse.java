package com.team23.customer.purchaseorder.dto;

import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;

public record ReceiveCancelResponse(
        Long receiveHistoryId,
        Long skuId,
        int cancelledQuantity,
        int currentStock,
        PurchaseOrderStatus purchaseOrderStatus
) {
}
