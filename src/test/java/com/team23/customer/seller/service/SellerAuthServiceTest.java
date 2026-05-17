package com.team23.customer.seller.service;

import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.auth.repository.RefreshTokenRepository;
import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.repository.SellerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SellerAuthServiceTest {
    @Autowired
    SellerAuthService sellerAuthService;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    private Seller createSeller(String loginId, String rawPassword) {
        return sellerRepository.save(
                Seller.create(
                        loginId,
                        passwordEncoder.encode(rawPassword),
                        null,
                        "주식회사 예시",
                        "홍길동",
                        "hong@example.com",
                        "02-1234-5678",
                        "010-1234-5678"
                )
        );
    }

    @Test
    @DisplayName("정상 로그인 — 토큰 발급")
    void loginSuccessReturnsTokenPair() {
        createSeller("seller001", "temp1234!");

        TokenPair tokens = sellerAuthService.login("seller001", "temp1234!");

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("없는 아이디 → 예외")
    void loginNotFoundThrowsException() {
        assertThatThrownBy(() ->
                sellerAuthService.login("unknown", "temp1234!")
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("비밀번호 불일치 → 예외")
    void loginWrongPasswordThrowsException() {
        createSeller("seller001", "temp1234!");

        assertThatThrownBy(() ->
                sellerAuthService.login("seller001", "wrongPassword")
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("SUSPENDED 계정 → 예외")
    void loginSuspendedThrowsException() {
        Seller seller = createSeller("seller001", "temp1234!");

        // 정지 처리 — 직접 SQL 또는 별도 메서드
        // seller.suspend() 메서드 있으면 사용
        seller.suspend();

        sellerRepository.save(seller);

        assertThatThrownBy(() ->
                sellerAuthService.login("seller001", "temp1234!")
        ).isInstanceOf(BusinessException.class);
    }


}