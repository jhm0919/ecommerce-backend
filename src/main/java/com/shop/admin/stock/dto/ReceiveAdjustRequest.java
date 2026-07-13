package com.shop.admin.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReceiveAdjustRequest(
        @Min(value = 1, message = "수정 수량은 1 이상이어야 합니다")
        int receivedQuantity,

        @NotBlank(message = "수정 사유는 필수입니다")
        String reason
) {

}
