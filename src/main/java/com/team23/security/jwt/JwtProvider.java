package com.team23.security.jwt;

import com.team23.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

    public JwtProvider(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenValidity = Duration.ofSeconds(properties.accessTokenValiditySeconds());
        this.refreshTokenValidity = Duration.ofSeconds(properties.refreshTokenValiditySeconds());
    }

    /**
     * Access Token 발급.
     *
     * @param memberId 내부 회원 ID (DB PK)
     * @param providerSub OAuth provider의 사용자 식별자 (sub claim에 사용)
     * @param role 회원의 권한 (USER/ADMIN)
     */
    public String createAccessToken(Long memberId, String providerSub, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenValidity);

        return Jwts.builder()
                .subject(providerSub)
                .claim("memberId", memberId)
                .claim("type", "access")
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 파싱 및 검증.
     *
     * @return 유효하면 Claims 반환
     * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않은 경우
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 현재 시점 기준 Refresh Token의 만료 시각을 계산한다.
     * yml의 refresh-token-validity-seconds 설정 기반.
     */
    public LocalDateTime getRefreshTokenExpiresAt() {
        return LocalDateTime.now().plus(refreshTokenValidity);
    }

    /**
     * 만료 여부만 체크 (참고용 헬퍼).
     */
    public boolean isExpired(String token) {
        try {
            parseAndValidate(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return true;
        }
    }
}