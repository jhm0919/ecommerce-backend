package com.team23.customer.auth.domain;

import com.team23.common.security.auth.domain.RefreshTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class RefreshTokenGeneratorTest {

    @Test
    @DisplayName("매번 다른 토큰을 생성한다 (고유성)")
    void generatesUniqueTokens() {
        Set<String> tokens = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            tokens.add(RefreshTokenGenerator.generate());
        }

        // 1000개 모두 달라야 함
        assertThat(tokens).hasSize(1000);
    }

    @Test
    @DisplayName("URL-safe Base64 형식이다")
    void isUrlSafeBase64() {
        String token = RefreshTokenGenerator.generate();

        // URL-safe Base64는 A-Z, a-z, 0-9, -, _ 만 포함
        assertThat(token).matches("^[A-Za-z0-9_-]+$");
    }

    @Test
    @DisplayName("토큰 길이가 약 43자 (32바이트 Base64URL 인코딩)")
    void tokenLengthIs43() {
        String token = RefreshTokenGenerator.generate();

        // 32바이트 → Base64 인코딩 시 ceil(32 * 4 / 3) = 43자 (padding 없음)
        assertThat(token).hasSize(43);
    }
}