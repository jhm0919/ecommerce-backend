package com.shop.admin.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceiveStockRequest (
        @NotNull
        Long purchaseOrderId,

        @Min(value = 1, message = "입고 수량은 1 이상이어야 합니다.")
        int receivedQuantity
) {
}
