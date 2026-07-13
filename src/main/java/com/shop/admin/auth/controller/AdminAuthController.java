package com.shop.admin.auth.controller;

import com.shop.global.security.auth.dto.TokenPair;
import com.shop.global.response.CommonResponse;
import com.shop.global.security.jwt.CookieIssuer;
import com.shop.admin.auth.dto.SellerLoginRequest;
import com.shop.admin.auth.dto.SellerLoginResponse;
import com.shop.admin.auth.repository.AdminRepository;
import com.shop.admin.auth.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "판매자 인증", description = "판매자 로그인 API")
@RestController
@RequestMapping("/api/seller/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminRepository adminRepository;
    private final CookieIssuer cookieIssuer;        // 재사용

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

    
}
