package com.team23.global.security.auth.dto;

/**
 * 클라이언트에 반환할 토큰 응답.
 *
 * <p>Access Token만 body에 포함된다.
 * Refresh Token은 HttpOnly 쿠키로 별도 전송되어 body에 노출되지 않는다.
 */
public record TokenResponse(
        String accessToken
) {
}
