package com.team23.global.security.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Refresh Token을 표현하는 엔티티.
 *
 * <p>실제 토큰 값은 저장하지 않고, SHA-256 해시만 저장한다.
 * DB 유출 시에도 토큰 원본 복구가 불가능하다.
 *
 * <p>토큰의 생명주기:
 * <ol>
 *   <li>로그인 시 발급 → 저장 (revokedAt = null)</li>
 *   <li>재발급 시 검증 → 유효성 확인</li>
 *   <li>로그아웃 시 무효화 → revokedAt = now</li>
 *   <li>만료 시 정리 → 배치 작업으로 삭제</li>
 * </ol>
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_rt_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_rt_member", columnList = "member_id"),
        @Index(name = "idx_rt_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, updatable = false)
    private Long memberId;

    @Column(name = "token_hash", nullable = false, unique = true, updatable = false, length = 64)
    private String tokenHash;  // SHA-256 = 64자 hex

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;  // null이면 유효

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    /**
     * 새 RefreshToken을 발급한다.
     *
     * @param memberId 회원 ID
     * @param tokenHash 토큰의 SHA-256 해시 (64자 hex)
     * @param expiresAt 만료 시각
     */
    public static RefreshToken createRefreshToken(Long memberId, String tokenHash, LocalDateTime expiresAt) {
        validateMemberId(memberId);
        validateTokenHash(tokenHash);
        validateExpiresAt(expiresAt);

        RefreshToken token = new RefreshToken();
        token.memberId = memberId;
        token.tokenHash = tokenHash;
        token.expiresAt = expiresAt;
        token.revokedAt = null;
        return token;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    /**
     * 토큰을 무효화한다 (로그아웃 시).
     */
    public void revoke() {
        if (this.revokedAt != null) {
            throw new IllegalStateException("Token already revoked");
        }
        this.revokedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    /**
     * 토큰이 현재 사용 가능한지 확인.
     * 무효화되지 않았고, 만료되지 않은 경우에만 true.
     */
    public boolean isUsable() {
        return !isRevoked() && !isExpired();
    }

    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateMemberId(Long memberId) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        if (memberId <= 0) {
            throw new IllegalArgumentException("memberId must be positive: " + memberId);
        }
    }

    private static void validateTokenHash(String tokenHash) {
        Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        if (tokenHash.length() != 64) {
            throw new IllegalArgumentException(
                    "tokenHash must be 64 characters (SHA-256 hex): got " + tokenHash.length());
        }
    }

    private static void validateExpiresAt(LocalDateTime expiresAt) {
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "expiresAt must be in the future: " + expiresAt);
        }
    }
}
