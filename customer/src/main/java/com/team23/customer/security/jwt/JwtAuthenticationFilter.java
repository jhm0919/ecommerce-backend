package com.team23.customer.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        // 토큰이 없으면 우리 필터가 관여하지 않음 (다른 필터에게 맡김)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 있으면 반드시 우리가 처리한다: 성공 or 명시적 거부
        try {
            Claims claims = jwtProvider.parseAndValidate(token);
            String tokenType = claims.get("type", String.class);

            if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
                // access 토큰이 아닌 경우 (refresh 등) → 명시적 거부
                log.warn("Token is not an access token: type={}", tokenType);
                SecurityContextHolder.clearContext();  // ★ 추가
            } else {
                // 정상 access 토큰 → 인증 설정
                setAuthentication(claims);
            }

        } catch (JwtException e) {
            // 파싱 실패, 만료, 서명 불일치 등 → 명시적 거부
            log.debug("Invalid JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();  // ★ 추가
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(Claims claims) {
        AuthPrincipal principal = new AuthPrincipal(
                claims.get("memberId", Long.class),
                claims.getSubject()
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
