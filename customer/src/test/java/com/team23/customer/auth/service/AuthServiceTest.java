package com.team23.customer.auth.service;

import com.team23.customer.auth.domain.RefreshToken;
import com.team23.customer.auth.domain.TokenHasher;
import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.auth.exception.InvalidRefreshTokenException;
import com.team23.customer.auth.repository.RefreshTokenRepository;
import com.team23.customer.member.domain.AuthProvider;
import com.team23.customer.member.domain.Member;
import com.team23.customer.member.repository.MemberRepository;
import com.team23.customer.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtProvider jwtProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private MemberRepository memberRepository;        // ★ 추가

    @InjectMocks private AuthService authService;

    private static final Long MEMBER_ID = 1L;
    private static final String PROVIDER_SUB = "google-sub-123";

    /**
     * 테스트용 Member 생성 헬퍼.
     */
    private Member createTestMember() {
        return Member.registerFromOAuth(
                AuthProvider.GOOGLE,
                PROVIDER_SUB,
                "test@example.com",
                true,
                "테스트사용자",
                null,
                "ko"
        );
    }

    @Nested
    @DisplayName("토큰 발급 (issueTokens)")
    class IssueTokens {

        @Test
        @DisplayName("AT와 RT를 발급하고 RT는 DB에 저장한다")
        void issueAndSave() {
            given(jwtProvider.createAccessToken(MEMBER_ID, PROVIDER_SUB))
                    .willReturn("mock-access-token");
            given(jwtProvider.getRefreshTokenExpiresAt())
                    .willReturn(LocalDateTime.now().plusDays(14));

            TokenPair result = authService.issueTokens(MEMBER_ID, PROVIDER_SUB);

            assertThat(result.accessToken()).isEqualTo("mock-access-token");
            assertThat(result.refreshToken()).isNotBlank();
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("매번 다른 RT 원본을 생성한다")
        void differentRefreshTokensEachTime() {
            given(jwtProvider.createAccessToken(any(), any())).willReturn("at");
            given(jwtProvider.getRefreshTokenExpiresAt())
                    .willReturn(LocalDateTime.now().plusDays(14));

            TokenPair first = authService.issueTokens(MEMBER_ID, PROVIDER_SUB);
            TokenPair second = authService.issueTokens(MEMBER_ID, PROVIDER_SUB);

            assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 (refreshTokens)")
    class RefreshTokens {

        @Test
        @DisplayName("유효한 RT로 새 토큰 쌍을 받을 수 있다")
        void refreshWithValidToken() {
            // given
            String rawRefreshToken = "valid-refresh-token";
            String tokenHash = TokenHasher.hash(rawRefreshToken);

            RefreshToken existingToken = RefreshToken.issue(
                    MEMBER_ID, tokenHash, LocalDateTime.now().plusDays(14)
            );
            Member member = createTestMember();

            given(refreshTokenRepository.findByTokenHash(tokenHash))
                    .willReturn(Optional.of(existingToken));
            given(memberRepository.findById(MEMBER_ID))               // ★ 추가
                    .willReturn(Optional.of(member));
            given(jwtProvider.createAccessToken(MEMBER_ID, PROVIDER_SUB))
                    .willReturn("new-access-token");
            given(jwtProvider.getRefreshTokenExpiresAt())
                    .willReturn(LocalDateTime.now().plusDays(14));

            // when
            TokenPair result = authService.refreshTokens(rawRefreshToken);  // ★ 인자 1개

            // then
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isNotEqualTo(rawRefreshToken);  // 새 RT
            assertThat(existingToken.isRevoked()).isTrue();  // 기존 RT 무효화
            verify(refreshTokenRepository).save(any(RefreshToken.class));  // 새 RT 저장
        }

        @Test
        @DisplayName("DB에 없는 RT는 거부")
        void rejectUnknownToken() {
            given(refreshTokenRepository.findByTokenHash(any()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshTokens("unknown"))   // ★ 인자 1개
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("이미 무효화된 RT는 거부")
        void rejectRevokedToken() {
            RefreshToken revokedToken = RefreshToken.issue(
                    MEMBER_ID, TokenHasher.hash("token"), LocalDateTime.now().plusDays(14)
            );
            revokedToken.revoke();

            given(refreshTokenRepository.findByTokenHash(any()))
                    .willReturn(Optional.of(revokedToken));

            assertThatThrownBy(() -> authService.refreshTokens("token"))     // ★ 인자 1개
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("null/빈 토큰은 거부")
        void rejectBlankToken() {
            assertThatThrownBy(() -> authService.refreshTokens(null))        // ★ 인자 1개
                    .isInstanceOf(InvalidRefreshTokenException.class);

            assertThatThrownBy(() -> authService.refreshTokens(""))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            assertThatThrownBy(() -> authService.refreshTokens("   "))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("RT는 유효하지만 Member가 없으면 거부 (이론적 안전망)")  // ★ 신규
        void rejectWhenMemberNotFound() {
            String rawToken = "valid-token";
            RefreshToken existingToken = RefreshToken.issue(
                    MEMBER_ID, TokenHasher.hash(rawToken), LocalDateTime.now().plusDays(14)
            );

            given(refreshTokenRepository.findByTokenHash(any()))
                    .willReturn(Optional.of(existingToken));
            given(memberRepository.findById(MEMBER_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshTokens(rawToken))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            // 기존 RT는 무효화되지 않아야 함 (Member 없는데 회전 X)
            assertThat(existingToken.isRevoked()).isFalse();
        }
    }

    @Nested
    @DisplayName("로그아웃 (logout)")
    class Logout {

        @Test
        @DisplayName("RT를 무효화한다")
        void logoutRevokesToken() {
            String rawToken = "valid-token";
            String hash = TokenHasher.hash(rawToken);
            RefreshToken token = RefreshToken.issue(
                    MEMBER_ID, hash, LocalDateTime.now().plusDays(14)
            );

            given(refreshTokenRepository.findByTokenHash(hash))
                    .willReturn(Optional.of(token));

            authService.logout(rawToken);

            assertThat(token.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("이미 무효화된 RT를 다시 로그아웃해도 에러 없음 (멱등성)")
        void idempotentLogout() {
            String rawToken = "valid-token";
            RefreshToken token = RefreshToken.issue(
                    MEMBER_ID, TokenHasher.hash(rawToken), LocalDateTime.now().plusDays(14)
            );
            token.revoke();

            given(refreshTokenRepository.findByTokenHash(any()))
                    .willReturn(Optional.of(token));

            assertThatCode(() -> authService.logout(rawToken))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("DB에 없는 RT를 로그아웃해도 에러 없음")
        void logoutUnknownTokenIsSilent() {
            given(refreshTokenRepository.findByTokenHash(any()))
                    .willReturn(Optional.empty());

            assertThatCode(() -> authService.logout("unknown"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null/빈 RT는 조용히 무시")
        void logoutBlankTokenIsSilent() {
            assertThatCode(() -> authService.logout(null)).doesNotThrowAnyException();
            assertThatCode(() -> authService.logout("")).doesNotThrowAnyException();
            assertThatCode(() -> authService.logout("   ")).doesNotThrowAnyException();

            verify(refreshTokenRepository, never()).findByTokenHash(any());
        }
    }
}