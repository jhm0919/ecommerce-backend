package com.team23.customer.purchaseorder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePurchaseOrderRequest(
        @NotNull(message = "SKU ID는 필수입니다")
        Long skuId,

        @Min(value = 1, message = "발주 수량은 1 이상이어야 합니다")
        int quantity,

        @NotBlank(message = "공급처명은 필수입니다")
        String supplierName,

        String supplierContact,   // 선택

        @NotNull(message = "입고 예정일은 필수입니다")
        @FutureOrPresent(message = "입고 예정일은 오늘 이후여야 합니다")
        LocalDate expectedAt
) {
}
