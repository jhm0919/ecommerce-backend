package com.team23.customer.seller.controller;

import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.global.response.CommonResponse;
import com.team23.customer.security.jwt.CookieIssuer;
import com.team23.customer.seller.dto.SellerLoginRequest;
import com.team23.customer.seller.dto.SellerLoginResponse;
import com.team23.customer.seller.repository.SellerRepository;
import com.team23.customer.seller.service.SellerAuthService;
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
public class SellerAuthController {

    private final SellerAuthService sellerAuthService;
    private final SellerRepository sellerRepository;
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
        TokenPair tokens = sellerAuthService.login(request.loginId(), request.password());

        // RT → HttpOnly 쿠키 (CookieIssuer 재사용)
        cookieIssuer.addRefreshTokenCookie(response, tokens.refreshToken());

        // 임시 비밀번호 여부
        boolean isTemporary = sellerRepository
                .findByLoginId(request.loginId())
                .orElseThrow()
                .isTemporaryPassword();

        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        new SellerLoginResponse(tokens.accessToken(), isTemporary)
                )
        );
    }
}
