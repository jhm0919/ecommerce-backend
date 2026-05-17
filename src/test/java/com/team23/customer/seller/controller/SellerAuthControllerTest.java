package com.team23.customer.seller.controller;

import com.team23.customer.auth.repository.RefreshTokenRepository;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.dto.SellerLoginRequest;
import com.team23.customer.seller.repository.SellerRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerAuthControllerTest {
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

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        sellerRepository.deleteAll();
        sellerRepository.save(
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
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/seller/auth/login — 정상 로그인 200")
    void loginSuccessReturns200() throws Exception {
        SellerLoginRequest request = new SellerLoginRequest("seller001", "temp1234!");

        mockMvc.perform(post("/api/seller/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.isTemporaryPassword").value(true))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("없는 아이디 → 401")
    void loginNotFoundReturns401() throws Exception {
        SellerLoginRequest request =
                new SellerLoginRequest("unknown", "temp1234!");

        mockMvc.perform(post("/api/seller/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("비밀번호 불일치 → 401")
    void loginWrongPasswordReturns401() throws Exception {
        SellerLoginRequest request =
                new SellerLoginRequest("seller001", "wrongPass");

        mockMvc.perform(post("/api/seller/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("필수 항목 누락 → 400")
    void loginMissingFieldReturns400() throws Exception {
        mockMvc.perform(post("/api/seller/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\": \"seller001\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}