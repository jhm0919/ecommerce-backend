package com.team23.customer.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeLoginIdRequest(
        @NotBlank(message = "새 아이디는 필수입니다")
        @Size(min = 4, max = 50, message = "아이디는 4~50자입니다")
        String newLoginId
) {
}
