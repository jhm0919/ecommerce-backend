package com.team23.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity
) {}
