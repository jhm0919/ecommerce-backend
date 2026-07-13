package com.team23.admin.purchaseorder.dto;

import com.team23.admin.purchaseorder.domain.PurchaseOrder;
import com.team23.admin.purchaseorder.domain.PurchaseOrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseOrderListResponse(
        Long purchaseOrderId,
        String purchaseOrderNumber,
        Long skuId,
        int quantity,
        String supplierName,
        LocalDate expectedAt,
        PurchaseOrderStatus status,
        LocalDateTime createdAt
) {
    public static PurchaseOrderListResponse from(PurchaseOrder po) {
        return new PurchaseOrderListResponse(
                po.getId(),
                po.getPurchaseOrderNumber(),
                po.getSkuId(),
                po.getQuantity(),
                po.getSupplierName(),
                po.getExpectedAt(),
                po.getStatus(),
                po.getCreatedAt()
        );
    }
}
