package com.team23.customer.auth.controller;

import com.team23.customer.auth.repository.RefreshTokenRepository;
import com.team23.customer.auth.domain.TokenHasher;
import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.auth.service.AuthService;
import com.team23.customer.member.domain.AuthProvider;
import com.team23.customer.member.domain.Member;
import com.team23.customer.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    private static final String COOKIE_NAME = "refreshToken";

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private ObjectMapper objectMapper;

    private Member testMember;
    private TokenPair initialTokens;


    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(
                Member.registerFromOAuth(
                        AuthProvider.GOOGLE,
                        "test-sub-" + System.currentTimeMillis(),
                        "test@example.com",
                        true,
                        "테스트사용자",
                        null,
                        "ko"
                )
        );
        initialTokens = authService.issueTokens(
                testMember.getId(),
                testMember.getProviderSub()
        );

        // ★ 임시 디버그
        String hash = TokenHasher.hash(initialTokens.refreshToken());
        System.out.println(">>> Token raw: " + initialTokens.refreshToken().substring(0, 10) + "...");
        System.out.println(">>> Token hash: " + hash);
        System.out.println(">>> Member ID: " + testMember.getId());

        boolean found = refreshTokenRepository.findByTokenHash(hash).isPresent();
        System.out.println(">>> RT in DB after setUp: " + found);

        long allCount = refreshTokenRepository.count();
        System.out.println(">>> Total RT count: " + allCount);
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("유효한 RT 쿠키로 새 토큰을 받을 수 있다")
        void refreshWithValidCookie() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(COOKIE_NAME, initialTokens.refreshToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists(COOKIE_NAME))
                    .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                    .andReturn();

            // ❌ 제거: AT 비교 (JWT 시간 정밀도로 인해 같을 수 있음)
            // JsonNode response = objectMapper.readTree(
            //         result.getResponse().getContentAsString());
            // String newAccessToken = response.get("accessToken").asText();
            // assertThat(newAccessToken).isNotEqualTo(initialTokens.accessToken());

            // ✅ 유지: 새 RT는 기존과 달라야 함 (RT는 SecureRandom이라 항상 다름)
            String newRefreshToken = result.getResponse()
                    .getCookie(COOKIE_NAME).getValue();
            assertThat(newRefreshToken).isNotEqualTo(initialTokens.refreshToken());

            // ✅ 유지: 기존 RT가 DB에서 무효화됨
            String oldHash = TokenHasher.hash(initialTokens.refreshToken());
            assertThat(refreshTokenRepository.findByTokenHash(oldHash))
                    .hasValueSatisfying(t -> assertThat(t.isRevoked()).isTrue());
        }

        @Test
        @DisplayName("RT 쿠키 없으면 401")
        void refreshWithoutCookie() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 RT는 401")
        void refreshWithInvalidCookie() throws Exception {
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(COOKIE_NAME, "invalid-token-value")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("이미 사용된 RT는 401 (회전)")
        void refreshTwiceWithSameToken() throws Exception {
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(COOKIE_NAME, initialTokens.refreshToken())))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie(COOKIE_NAME, initialTokens.refreshToken())))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("RT가 무효화되고 쿠키가 삭제된다")
        void logoutInvalidatesTokenAndClearsCookie() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie(COOKIE_NAME, initialTokens.refreshToken())))
                    .andExpect(status().isNoContent())
                    .andExpect(cookie().maxAge(COOKIE_NAME, 0));

            String hash = TokenHasher.hash(initialTokens.refreshToken());
            assertThat(refreshTokenRepository.findByTokenHash(hash))
                    .hasValueSatisfying(t -> assertThat(t.isRevoked()).isTrue());
        }

        @Test
        @DisplayName("쿠키 없어도 정상 (멱등성)")
        void logoutWithoutCookie() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("이미 로그아웃된 RT로 다시 로그아웃해도 정상 (멱등성)")
        void logoutTwice() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie(COOKIE_NAME, initialTokens.refreshToken())))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie(COOKIE_NAME, initialTokens.refreshToken())))
                    .andExpect(status().isNoContent());
        }
    }
}