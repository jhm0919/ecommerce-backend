package com.shop.admin.order.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderAdminCancelRequest(
        @NotBlank(message = "취소 사유는 필수입니다")
        String cancelReason,

        @NotBlank(message = "취소 사유 코드는 필수입니다")
        String cancelReasonCode
) {

}
