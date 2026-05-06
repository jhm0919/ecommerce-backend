package com.team23.customer.purchaseorder.dto;

import java.time.LocalDate;

public record PurchaseOrderRequest(
        Long skuId,
        int quantity,
        String supplierName,
        String supplierContact,
        LocalDate expectedAt
) {
}
