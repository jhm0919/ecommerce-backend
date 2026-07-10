package com.team23.customer.seller.controller;

import com.team23.common.security.auth.dto.TokenPair;
import com.team23.common.security.auth.repository.RefreshTokenRepository;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.dto.ChangeLoginIdRequest;
import com.team23.customer.seller.dto.ChangePasswordRequest;
import com.team23.customer.seller.dto.UpdateProfileRequest;
import com.team23.customer.seller.repository.SellerRepository;
import com.team23.customer.seller.service.SellerAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SellerControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    SellerAuthService sellerAuthService; // 토큰 발급용

    private Seller testSeller;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        refreshTokenRepository.deleteAll();
        sellerRepository.deleteAll();

        testSeller = sellerRepository.save(
                Seller.create(
                        "seller001",
                        passwordEncoder.encode("temp1234!"),
                        null,
                        "주식회사 예시",
                        "홍길동",
                        "hong@example.com",
                        "02-1234-5678",
                        "010-1234-5678"
                )
        );

        // 로그인해서 AT 발급
        TokenPair tokens = sellerAuthService.login("seller001", "temp1234!");
        accessToken = tokens.accessToken();
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @Test
    @DisplayName("PATCH /me/login-id - 정상 200")
    void changeLoginIdSuccessReturns200() throws Exception {
        ChangeLoginIdRequest request = new ChangeLoginIdRequest("newId001");

        mockMvc.perform(patch("/api/seller/me/login-id")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /me/login-id - 미인증 → 401")
    void changeLoginIdUnauthorizedReturns401() throws Exception {
        mockMvc.perform(patch("/api/seller/me/login-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newLoginId\":\"x\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /me/password - 정상 200")
    void changePasswordSuccessReturns200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("temp1234!", "newPass123!");

        mockMvc.perform(patch("/api/seller/me/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /me/password - 현재 비밀번호 틀림 → 400")
    void changePasswordWrongCurrentReturns400() throws Exception {
        ChangePasswordRequest request =
                new ChangePasswordRequest("wrongPass", "newPass123!");

        mockMvc.perform(patch("/api/seller/me/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /me/profile - 정상 200")
    void updateProfileSuccessReturns200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "새 상호명", "김담당", "new@example.com",
                "02-9999-9999", "010-9999-9999"
        );

        mockMvc.perform(patch("/api/seller/me/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /me/profile - 이메일 형식 오류 → 400")
    void updateProfileInvalidEmailReturns400() throws Exception {
        String body = """
                {
                  "businessName": "새 상호명",
                  "managerName": "김담당",
                  "managerEmail": "not-an-email",
                  "phoneNumber": "02-9999-9999",
                  "mobileNumber": "010-9999-9999"
                }
                """;

        mockMvc.perform(patch("/api/seller/me/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}