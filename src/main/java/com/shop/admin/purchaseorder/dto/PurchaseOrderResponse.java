package com.shop.admin.purchaseorder.dto;

import com.shop.admin.purchaseorder.domain.PurchaseOrder;

public record PurchaseOrderResponse(
        Long purchaseOrderId,
        String purchaseOrderNumber
) {
    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(po.getId(), po.getPurchaseOrderNumber());
    }
}
