package com.team23.customer.seller.service;

import com.team23.customer.auth.repository.RefreshTokenRepository;
import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.dto.ChangePasswordRequest;
import com.team23.customer.seller.dto.UpdateProfileRequest;
import com.team23.customer.seller.repository.SellerRepository;
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
class SellerServiceTest {

    @Autowired
    SellerService sellerService;
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

    // ───── 아이디 변경 ─────

    @Test
    @DisplayName("아이디 변경 성공")
    void changeLoginIdSuccess() {
        Seller seller = createSeller("seller001", "temp1234!");

        sellerService.changeLoginId(seller.getId(), "newId001");

        Seller updated = sellerRepository.findById(seller.getId()).orElseThrow();
        assertThat(updated.getLoginId()).isEqualTo("newId001");
    }

    @Test
    @DisplayName("아이디 변경 - 중복 → 예외")
    void changeLoginIdDuplicateThrowsException() {
        Seller seller = createSeller("seller001", "temp1234!");
        sellerRepository.save(
                Seller.create("existingId", passwordEncoder.encode("pw"),
                        null, "다른업체", "김철수", "kim@test.com",
                        "02-0000-0000", "010-0000-0000")
        );

        assertThatThrownBy(() ->
                sellerService.changeLoginId(seller.getId(), "existingId")
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("아이디 변경 - 현재 아이디와 동일 → 예외")
    void changeLoginIdSameThrowsException() {
        Seller seller = createSeller("seller001", "temp1234!");

        assertThatThrownBy(() ->
                sellerService.changeLoginId(seller.getId(), "seller001")
        ).isInstanceOf(BusinessException.class);
    }

    // ───── 비밀번호 변경 ─────

    @Test
    @DisplayName("비밀번호 변경 성공 - 임시 비밀번호 해제")
    void changePasswordSuccessClearsTemporaryFlag() {
        Seller seller = createSeller("seller001", "temp1234!");
        assertThat(seller.isTemporaryPassword()).isTrue();

        sellerService.changePassword(seller.getId(),
                new ChangePasswordRequest("temp1234!", "newPass123!"));

        Seller updated = sellerRepository.findById(seller.getId()).orElseThrow();
        assertThat(updated.isTemporaryPassword()).isFalse();
        assertThat(passwordEncoder.matches("newPass123!", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호 불일치 → 예외")
    void changePasswordWrongCurrentThrowsException() {
        Seller seller = createSeller("seller001", "temp1234!");

        assertThatThrownBy(() ->
                sellerService.changePassword(seller.getId(),
                        new ChangePasswordRequest("wrongPass", "newPass123!"))
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호가 현재와 동일 → 예외")
    void changePasswordSameAsCurrentThrowsException() {
        Seller seller = createSeller("seller001", "temp1234!");

        assertThatThrownBy(() ->
                sellerService.changePassword(seller.getId(),
                        new ChangePasswordRequest("temp1234!", "temp1234!"))
        ).isInstanceOf(BusinessException.class);
    }

    // ───── 운영 정보 변경 ─────

    @Test
    @DisplayName("운영 정보 변경 성공")
    void updateProfileSuccess() {
        Seller seller = createSeller("seller001", "temp1234!");

        sellerService.updateProfile(seller.getId(),
                new UpdateProfileRequest(
                        "새 상호명",
                        "김담당",
                        "new@example.com",
                        "02-9999-9999",
                        "010-9999-9999"
                ));

        Seller updated = sellerRepository.findById(seller.getId()).orElseThrow();
        assertThat(updated.getBusinessName()).isEqualTo("새 상호명");
        assertThat(updated.getManagerName()).isEqualTo("김담당");
        assertThat(updated.getManagerEmail()).isEqualTo("new@example.com");
        assertThat(updated.getPhoneNumber()).isEqualTo("02-9999-9999");
        assertThat(updated.getMobileNumber()).isEqualTo("010-9999-9999");
    }

}