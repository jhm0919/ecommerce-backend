package com.shop.global.security.auth.controller;

import com.shop.global.security.auth.dto.TokenPair;
import com.shop.global.response.CommonResponse;
import com.shop.global.security.auth.dto.TokenResponse;
import com.shop.global.security.jwt.CookieIssuer;
import com.shop.global.security.auth.dto.AuthLoginRequest;
import com.shop.global.security.auth.dto.AuthLoginResponse;
import com.shop.global.security.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "토큰 갱신 및 로그인/로그아웃 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final CookieIssuer cookieIssuer;        // 재사용


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
            summary = "로그인",
            description = "아이디/비밀번호로 로그인. RT는 HttpOnly 쿠키, AT는 body 반환."
    )
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<AuthLoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokens = authService.login(request.email(), request.password());

        // RT → HttpOnly 쿠키 (CookieIssuer 재사용)
        cookieIssuer.addRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        new AuthLoginResponse(tokens.accessToken())
                )
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
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        cookieIssuer.removeRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

}
