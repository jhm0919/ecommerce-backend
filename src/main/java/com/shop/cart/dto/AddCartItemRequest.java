package com.shop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니에 상품 추가 요청.
 */
public record AddCartItemRequest(
        @NotNull(message = "상품 ID는 필수입니다")
        Long productId,

        @NotNull(message = "SKU ID는 필수입니다")  // ★ 추가
        Long skuId,

        @NotNull
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity
) {
}