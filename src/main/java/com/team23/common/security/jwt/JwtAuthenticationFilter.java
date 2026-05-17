package com.team23.common.security.jwt;

import com.team23.customer.member.domain.MemberRole;
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

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtProvider.parseAndValidate(token);
            String tokenType = claims.get("type", String.class);

            if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
                log.warn("Token is not an access token: type={}", tokenType);
                SecurityContextHolder.clearContext();
            } else {
                setAuthentication(claims);
            }

        } catch (JwtException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(Claims claims) {
        // ★ role 추출 (옛 토큰 호환을 위해 USER 기본값)
        MemberRole role = extractRole(claims);

        AuthPrincipal principal = new AuthPrincipal(
                claims.get("memberId", Long.class),
                claims.getSubject(),
                role  // ★ 추가
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        // ★ role 기반 권한 부여
                        List.of(new SimpleGrantedAuthority(role.toSpringSecurityRole()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * JWT claims에서 role을 추출한다.
     *
     * <p>role claim이 없거나 알 수 없는 값이면 USER를 기본값으로 사용한다.
     * (옛 형식의 토큰 호환을 위함)
     */
    private MemberRole extractRole(Claims claims) {
        String roleString = claims.get("role", String.class);
        if (roleString == null) {
            log.debug("Token missing role claim, defaulting to USER");
            return MemberRole.USER;
        }

        try {
            return MemberRole.valueOf(roleString);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role in token: {}, defaulting to USER", roleString);
            return MemberRole.USER;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}