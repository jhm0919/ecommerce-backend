package com.shop.admin.auth.controller;

import com.shop.global.security.auth.dto.TokenPair;
import com.shop.global.response.CommonResponse;
import com.shop.global.security.auth.service.AuthService;
import com.shop.global.security.jwt.CookieIssuer;
import com.shop.admin.auth.dto.SellerLoginRequest;
import com.shop.admin.auth.dto.SellerLoginResponse;
import com.shop.admin.auth.repository.AdminRepository;
import com.shop.admin.auth.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "판매자 인증", description = "판매자 로그인 API")
@RestController
@RequestMapping("/api/seller/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final CookieIssuer cookieIssuer;        // 재사용
    private final AuthService authService;


    @Operation(
            summary = "판매자 로그인",
            description = "아이디/비밀번호로 로그인. RT는 HttpOnly 쿠키, AT는 body 반환."
    )
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<SellerLoginResponse>> login(
            @Valid @RequestBody SellerLoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokens = adminAuthService.login(request.username(), request.password());

        // RT → HttpOnly 쿠키 (CookieIssuer 재사용)
        cookieIssuer.addRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        new SellerLoginResponse(tokens.accessToken())
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
