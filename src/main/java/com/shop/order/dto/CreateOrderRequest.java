package com.shop.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 주문 생성 요청.
 */
public record CreateOrderRequest(
        @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "배송 정보는 필수입니다")
        @Valid
        DeliveryInfoRequest delivery
) {
    public record OrderItemRequest(
            @NotNull(message = "상품 ID는 필수입니다")
            Long productId,

            @NotNull(message = "SKU ID는 필수입니다")  // ★ 추가
            Long skuId,

            @NotNull
            @Min(value = 1, message = "수량은 1 이상이어야 합니다")
            Integer quantity
    ) {}

    public record DeliveryInfoRequest(
            @NotBlank(message = "받는 사람 이름은 필수입니다")
            String receiverName,

            @NotBlank(message = "연락처는 필수입니다")
            String receiverPhone,

            @NotBlank(message = "우편번호는 필수입니다")
            String zipCode,

            @NotBlank(message = "주소는 필수입니다")
            String addressLine1,

            String addressLine2,

            String memo
    ) {}
}
