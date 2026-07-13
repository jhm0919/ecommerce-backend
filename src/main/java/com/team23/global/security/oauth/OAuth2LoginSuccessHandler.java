package com.team23.global.security.oauth;


import com.team23.global.security.auth.dto.TokenPair;
import com.team23.global.security.auth.service.AuthService;
import com.team23.member.domain.AuthProvider;
import com.team23.member.domain.Member;
import com.team23.member.exception.MemberNotFoundException;
import com.team23.member.repository.MemberRepository;
import com.team23.global.security.jwt.CookieIssuer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final AuthService authService;        // ★ 추가
    private final CookieIssuer cookieIssuer;

    @Value("${app.frontend.redirect-url:http://localhost:3000/auth/callback}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // 1. provider/sub 식별
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        AuthProvider provider = resolveProvider(oauthToken.getAuthorizedClientRegistrationId());
        String providerSub = extractProviderSub(oauthToken, provider);

        // 2. Member 조회
        Member member = memberRepository
                .findByAuthProviderAndProviderSub(provider, providerSub)
                .orElseThrow(() -> new MemberNotFoundException(providerSub));

        // 3. 토큰 발급 (AuthService에 위임)  ★ 핵심 변경
        TokenPair tokens = authService.createToken(member.getId(), providerSub, member.getRole().name());

        // 4. RT를 쿠키로
        cookieIssuer.addRefreshTokenCookie(response, tokens.refreshToken());

        // 5. AT를 fragment로 redirect
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .fragment("accessToken=" + tokens.accessToken())
                .build()
                .toUriString();

        log.info("OAuth login success: provider={}, memberId={}", provider, member.getId());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private AuthProvider resolveProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "kakao" -> AuthProvider.KAKAO;
            case "naver" -> AuthProvider.NAVER;
            default -> throw new IllegalArgumentException(
                    "Unsupported OAuth provider: " + registrationId);
        };
    }

    private String extractProviderSub(OAuth2AuthenticationToken token, AuthProvider provider) {
        OAuth2User principal = (OAuth2User) token.getPrincipal();

        return switch (provider) {
            case GOOGLE -> principal.getName();
            case KAKAO -> String.valueOf(principal.getAttributes().get("id"));
            case NAVER -> {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> response =
                        (java.util.Map<String, Object>) principal.getAttributes().get("response");
                yield (String) response.get("id");
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}