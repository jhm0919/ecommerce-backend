package com.team23.auth.domain;

import com.team23.global.security.auth.domain.TokenHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TokenHasherTest {

    @Test
    @DisplayName("같은 입력은 같은 해시를 만든다 (결정성)")
    void deterministicHash() {
        String token = "my-secret-token";

        String hash1 = TokenHasher.hash(token);
        String hash2 = TokenHasher.hash(token);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("다른 입력은 다른 해시를 만든다")
    void differentInputsDifferentHashes() {
        String hash1 = TokenHasher.hash("token-a");
        String hash2 = TokenHasher.hash("token-b");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("해시는 항상 64자 hex 문자열이다")
    void hashIsAlways64HexChars() {
        String shortInput = "a";
        String longInput = "a".repeat(10000);

        assertThat(TokenHasher.hash(shortInput))
                .hasSize(64)
                .matches("^[0-9a-f]{64}$");

        assertThat(TokenHasher.hash(longInput))
                .hasSize(64)
                .matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("null 입력은 거부")
    void rejectNull() {
        assertThatThrownBy(() -> TokenHasher.hash(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("빈 문자열 입력은 거부")
    void rejectBlank() {
        assertThatThrownBy(() -> TokenHasher.hash(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> TokenHasher.hash("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("UTF-8 한글도 처리 가능")
    void handleKoreanCharacters() {
        String korean = "한글토큰테스트";

        String hash = TokenHasher.hash(korean);

        assertThat(hash).hasSize(64);
    }
}