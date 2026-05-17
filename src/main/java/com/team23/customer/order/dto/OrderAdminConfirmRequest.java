package com.team23.customer.order.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderAdminConfirmRequest(
        @NotEmpty(message = "주문 ID 목록은 필수입니다")
        List<Long> orderIds
) {
}
