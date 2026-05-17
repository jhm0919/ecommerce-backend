package com.team23.customer.auth.service;

import com.team23.customer.auth.domain.RefreshToken;
import com.team23.customer.auth.domain.RefreshTokenGenerator;
import com.team23.customer.auth.domain.TokenHasher;
import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.auth.exception.InvalidRefreshTokenException;
import com.team23.customer.auth.repository.RefreshTokenRepository;
import com.team23.customer.member.domain.Member;
import com.team23.customer.member.repository.MemberRepository;
import com.team23.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;  // ★ 추가

    /**
     * 새로운 토큰 쌍을 발급하고 RT를 DB에 저장한다.
     * OAuth 로그인 성공 시 호출된다.
     */
    @Transactional
    public TokenPair issueTokens(Long memberId, String providerSub, String role) {
        String accessToken = jwtProvider.createAccessToken(memberId, providerSub, role);

        String rawRefreshToken = RefreshTokenGenerator.generate();
        String tokenHash = TokenHasher.hash(rawRefreshToken);
        LocalDateTime expiresAt = jwtProvider.getRefreshTokenExpiresAt();

        RefreshToken refreshTokenEntity = RefreshToken.issue(memberId, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("Tokens issued for memberId={}", memberId);
        return new TokenPair(accessToken, rawRefreshToken);
    }

    /**
     * Refresh Token으로 새 토큰 쌍을 재발급한다.
     *
     * <p>회전(Rotation): 사용된 RT는 즉시 무효화되고 새 RT가 발급된다.
     */
    @Transactional
    public TokenPair refreshTokens(String rawRefreshToken) {  // ★ providerSub 제거
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        // 1. RT 검증
        String tokenHash = TokenHasher.hash(rawRefreshToken);
        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!existingToken.isUsable()) {
            log.warn("Attempt to use invalid RT: memberId={}, revoked={}, expired={}",
                    existingToken.getMemberId(),
                    existingToken.isRevoked(),
                    existingToken.isExpired());
            throw new InvalidRefreshTokenException();
        }

        Long memberId = existingToken.getMemberId();

        // 2. Member 조회로 providerSub 확보
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("Member not found for valid RT: memberId={}", memberId);
                    return new InvalidRefreshTokenException();  // 보안: 같은 예외
                });

        // 3. 기존 RT 무효화 (회전)
        existingToken.revoke();

        // 4. 새 토큰 쌍 발급
        log.info("Refresh tokens for memberId={}", memberId);
        return issueTokens(memberId, member.getProviderSub(), member.getRole().name());
    }

    /**
     * Refresh Token을 무효화하여 로그아웃 처리한다.
     * 멱등성: 여러 번 호출해도 안전하다.
     */
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            log.debug("Logout called without refresh token");
            return;
        }

        String tokenHash = TokenHasher.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    if (!token.isRevoked()) {
                        token.revoke();
                        log.info("Logout: RT revoked for memberId={}", token.getMemberId());
                    }
                });
    }
}