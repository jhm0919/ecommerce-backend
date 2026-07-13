package com.team23.seller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "상호명은 필수입니다")
        String businessName,

        @NotBlank(message = "담당자명은 필수입니다")
        String managerName,

        @NotBlank(message = "담당자 이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String managerEmail,

        @NotBlank(message = "유선전화번호는 필수입니다")
        String phoneNumber,

        @NotBlank(message = "휴대전화번호는 필수입니다")
        String mobileNumber
) {
}
