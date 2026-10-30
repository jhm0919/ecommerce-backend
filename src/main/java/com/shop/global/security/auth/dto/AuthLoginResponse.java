package com.shop.global.security.auth.dto;

public record AuthLoginResponse(
        String accessToken
//        boolean isTemporaryPassword
) {
}
