package com.team23.customer.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class RefreshTokenTest {

    private static final String VALID_HASH =
            "a".repeat(64);  // 64자 hex 가짜 해시

    private RefreshToken issueValidToken() {
        return RefreshToken.issue(
                1L,
                VALID_HASH,
                LocalDateTime.now().plusDays(14)
        );
    }

    @Nested
    @DisplayName("발급 (issue)")
    class Issue {

        @Test
        @DisplayName("정상적인 RefreshToken을 발급할 수 있다")
        void issueValid() {
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

            RefreshToken token = RefreshToken.issue(1L, VALID_HASH, expiresAt);

            assertThat(token.getMemberId()).isEqualTo(1L);
            assertThat(token.getTokenHash()).isEqualTo(VALID_HASH);
            assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(token.getRevokedAt()).isNull();
        }

        @Test
        @DisplayName("memberId가 null이면 예외")
        void rejectNullMemberId() {
            assertThatThrownBy(() -> RefreshToken.issue(
                    null, VALID_HASH, LocalDateTime.now().plusDays(14)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("memberId가 0 이하면 예외")
        void rejectInvalidMemberId() {
            LocalDateTime future = LocalDateTime.now().plusDays(14);

            assertThatThrownBy(() -> RefreshToken.issue(0L, VALID_HASH, future))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> RefreshToken.issue(-1L, VALID_HASH, future))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("tokenHash가 64자가 아니면 예외")
        void rejectInvalidTokenHashLength() {
            LocalDateTime future = LocalDateTime.now().plusDays(14);

            assertThatThrownBy(() -> RefreshToken.issue(1L, "short", future))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("64 characters");

            assertThatThrownBy(() -> RefreshToken.issue(1L, "a".repeat(63), future))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> RefreshToken.issue(1L, "a".repeat(65), future))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("expiresAt이 과거면 예외")
        void rejectPastExpiresAt() {
            LocalDateTime past = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(() -> RefreshToken.issue(1L, VALID_HASH, past))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("future");
        }
    }

    @Nested
    @DisplayName("무효화 (revoke)")
    class Revoke {

        @Test
        @DisplayName("토큰을 무효화할 수 있다")
        void revokeToken() {
            RefreshToken token = issueValidToken();
            assertThat(token.isRevoked()).isFalse();

            token.revoke();

            assertThat(token.isRevoked()).isTrue();
            assertThat(token.getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 무효화된 토큰은 다시 무효화할 수 없다")
        void cannotRevokeAlreadyRevoked() {
            RefreshToken token = issueValidToken();
            token.revoke();

            assertThatThrownBy(token::revoke)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already revoked");
        }
    }

    @Nested
    @DisplayName("사용 가능 여부 (isUsable)")
    class Usability {

        @Test
        @DisplayName("발급 직후의 토큰은 사용 가능")
        void freshTokenIsUsable() {
            RefreshToken token = issueValidToken();

            assertThat(token.isUsable()).isTrue();
        }

        @Test
        @DisplayName("무효화된 토큰은 사용 불가")
        void revokedTokenIsNotUsable() {
            RefreshToken token = issueValidToken();
            token.revoke();

            assertThat(token.isUsable()).isFalse();
        }

        @Test
        @DisplayName("만료된 토큰은 사용 불가")
        void expiredTokenIsNotUsable() {
            RefreshToken token = RefreshToken.issue(
                    1L, VALID_HASH, LocalDateTime.now().plusDays(14)
            );

            // 만료 시점을 과거로 강제 (Reflection)
            ReflectionTestUtils.setField(
                    token, "expiresAt", LocalDateTime.now().minusSeconds(1)
            );

            assertThat(token.isExpired()).isTrue();
            assertThat(token.isUsable()).isFalse();
        }
    }
}