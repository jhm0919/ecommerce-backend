package com.team23.seller.controller;

import com.team23.seller.domain.SellerApplication;
import com.team23.seller.dto.SellerApplicationRequest;
import com.team23.seller.repository.SellerApplicationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SellerApplicationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired SellerApplicationRepository sellerApplicationRepository;

    @BeforeEach
    void setUp() {
        sellerApplicationRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        sellerApplicationRepository.deleteAll();
    }

    private SellerApplicationRequest createRequest(
            String brn, String mosn
    ) {
        return new SellerApplicationRequest(
                "주식회사 예시", brn, mosn,
                "도매 및 소매업", "의류",
                "홍길동", "hong@example.com",
                "02-1234-5678", "010-1234-5678"
        );
    }

    @Test
    @DisplayName("POST /api/seller/applications — 정상 신청 201")
    void applySuccessReturns201() throws Exception {
        SellerApplicationRequest request =
                createRequest("1234567890", "2024-서울강남-0001");

        mockMvc.perform(post("/api/seller/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.applicationId").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("사업자등록번호 중복 → 409")
    void applyDuplicateBrnReturns409() throws Exception {
        sellerApplicationRepository.save(
                SellerApplication.apply(
                        "기존 업체", "1234567890", "2024-서울강남-0001",
                        "도매", "의류", "김철수", "kim@example.com",
                        "02-0000-0000", "010-0000-0000"
                )
        );

        SellerApplicationRequest request =
                createRequest("1234567890", "2024-서울강남-9999");

        mockMvc.perform(post("/api/seller/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("필수 항목 누락 → 400")
    void applyMissingFieldReturns400() throws Exception {
        // businessName 누락
        String body = """
            {
              "businessRegistrationNumber": "1234567890",
              "mailOrderSalesNumber": "2024-서울강남-0001",
              "businessType": "도매",
              "businessCategory": "의류",
              "managerName": "홍길동",
              "managerEmail": "hong@example.com",
              "phoneNumber": "02-1234-5678",
              "mobileNumber": "010-1234-5678"
            }
            """;

        mockMvc.perform(post("/api/seller/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사업자등록번호 형식 오류 → 400")
    void applyInvalidBrnFormatReturns400() throws Exception {
        SellerApplicationRequest request =
                createRequest("123-45-6789", "2024-서울강남-0001"); // 하이픈 포함

        mockMvc.perform(post("/api/seller/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("이메일 형식 오류 → 400")
    void applyInvalidEmailReturns400() throws Exception {
        String body = """
            {
              "businessName": "주식회사 예시",
              "businessRegistrationNumber": "1234567890",
              "mailOrderSalesNumber": "2024-서울강남-0001",
              "businessType": "도매",
              "businessCategory": "의류",
              "managerName": "홍길동",
              "managerEmail": "not-an-email",
              "phoneNumber": "02-1234-5678",
              "mobileNumber": "010-1234-5678"
            }
            """;

        mockMvc.perform(post("/api/seller/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}