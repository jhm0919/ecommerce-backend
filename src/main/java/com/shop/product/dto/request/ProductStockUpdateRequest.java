package com.shop.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 재고 수정 요청 (입고/출고).
 */
public record ProductStockUpdateRequest(
        @NotNull
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity
) {
}
