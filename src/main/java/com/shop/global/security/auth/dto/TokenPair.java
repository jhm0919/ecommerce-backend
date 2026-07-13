package com.shop.global.security.auth.dto;

/**
 * 발급된 Access Token과 Refresh Token의 쌍.
 */
public record TokenPair(
        String accessToken,
        String refreshToken
) {
}