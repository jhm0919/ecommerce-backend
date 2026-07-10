package com.team23.common.security.auth.controller;

import com.team23.common.security.auth.dto.TokenPair;
import com.team23.common.security.auth.dto.TokenResponse;
import com.team23.common.security.auth.service.AuthService;
import com.team23.common.response.CommonResponse;
import com.team23.common.security.jwt.CookieIssuer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "토큰 갱신 및 로그아웃 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final CookieIssuer cookieIssuer;

    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token으로 새 Access Token과 Refresh Token을 발급받는다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 없음 또는 만료")
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenResponse>> refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenPair tokens = authService.refreshTokens(refreshToken);
        cookieIssuer.addRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(new TokenResponse(tokens.accessToken()))
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화하고 쿠키를 삭제한다. RT 없어도 정상 응답 (멱등)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    })
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