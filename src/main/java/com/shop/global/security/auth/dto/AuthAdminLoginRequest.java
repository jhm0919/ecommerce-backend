package com.shop.global.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthAdminLoginRequest(
        @NotBlank(message = "아이디는 필수입니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
}
