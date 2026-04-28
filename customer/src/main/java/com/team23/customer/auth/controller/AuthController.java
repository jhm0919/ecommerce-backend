package com.team23.customer.auth.controller;

import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.auth.dto.TokenResponse;
import com.team23.customer.auth.service.AuthService;
import com.team23.customer.security.jwt.CookieIssuer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final CookieIssuer cookieIssuer;

    /**
     * Refresh Token으로 새 토큰 쌍을 발급받는다.
     *
     * <p>흐름:
     * <ol>
     *   <li>쿠키에서 RT 추출</li>
     *   <li>AuthService에서 RT 검증 + 새 토큰 발급 + 회전</li>
     *   <li>새 RT는 쿠키로, 새 AT는 응답 body로</li>
     * </ol>
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenPair tokens = authService.refreshTokens(refreshToken);

        cookieIssuer.addRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(new TokenResponse(tokens.accessToken()));
    }

    /**
     * 로그아웃 처리.
     *
     * <p>RT가 없거나 이미 무효화되었어도 정상 응답 (멱등성).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        cookieIssuer.removeRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}