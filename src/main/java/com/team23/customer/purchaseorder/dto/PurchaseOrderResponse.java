package com.team23.customer.purchaseorder.dto;

import com.team23.customer.purchaseorder.domain.PurchaseOrder;

public record PurchaseOrderResponse(
        Long purchaseOrderId,
        String purchaseOrderNumber
) {
    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(po.getId(), po.getPurchaseOrderNumber());
    }
}
