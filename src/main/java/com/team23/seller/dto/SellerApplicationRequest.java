package com.team23.seller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SellerApplicationRequest(
        @NotBlank(message = "상호명은 필수입니다")
        String businessName,

        @NotBlank(message = "사업자등록번호는 필수입니다")
        @Pattern(regexp = "^\\d{10}$", message = "사업자등록번호는 10자리 숫자입니다")
        String businessRegistrationNumber,

        @NotBlank(message = "통신판매업신고번호는 필수입니다")
        String mailOrderSalesNumber,

        @NotBlank(message = "업태는 필수입니다")
        String businessType,

        @NotBlank(message = "업종은 필수입니다")
        String businessCategory,

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
