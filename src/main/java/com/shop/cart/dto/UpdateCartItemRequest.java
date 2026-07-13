package com.shop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 항목 수량 변경 요청.
 */
public record UpdateCartItemRequest(
        @NotNull
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity
) {
}
