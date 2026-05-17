package com.team23.management.seller.controller;

import tools.jackson.databind.ObjectMapper;
import com.team23.customer.seller.domain.SellerApplication;
import com.team23.customer.seller.repository.SellerApplicationRepository;
import com.team23.customer.seller.repository.SellerRepository;
import com.team23.management.seller.dto.RejectApplicationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SellerApplicationAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SellerApplicationRepository sellerApplicationRepository;
    @Autowired SellerRepository sellerRepository;

    @BeforeEach
    void setUp() {
        sellerRepository.deleteAll();
        sellerApplicationRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        sellerRepository.deleteAll();
        sellerApplicationRepository.deleteAll();
    }

    private SellerApplication createApplication(String brn, String mosn) {
        return sellerApplicationRepository.save(
                SellerApplication.apply(
                        "주식회사 예시", brn, mosn,
                        "도매 및 소매업", "의류",
                        "홍길동", "hong@example.com",
                        "02-1234-5678", "010-1234-5678"
                )
        );
    }

    @Test
    @DisplayName("GET /api/admin/seller/applications - 목록 조회 200")
    void listReturns200() throws Exception {
        createApplication("1111111111", "2024-A-0001");

        mockMvc.perform(get("/api/admin/seller/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/admin/seller/applications?status=PENDING - 필터")
    void listWithStatusReturns200() throws Exception {
        createApplication("1111111111", "2024-A-0001");

        mockMvc.perform(get("/api/admin/seller/applications")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/admin/seller/applications/{id}/approve - 승인 200 + 임시 자격증명")
    void approveReturns200() throws Exception {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");

        mockMvc.perform(post("/api/admin/seller/applications/{id}/approve", app.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.applicationId").value(app.getId()))
                .andExpect(jsonPath("$.data.sellerId").exists())
                .andExpect(jsonPath("$.data.temporaryLoginId").exists())
                .andExpect(jsonPath("$.data.temporaryPassword").exists());
    }

    @Test
    @DisplayName("존재하지 않는 applicationId 승인 → 404")
    void approveNotFoundReturns404() throws Exception {
        mockMvc.perform(post("/api/admin/seller/applications/999/approve")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/admin/seller/applications/{id}/reject - 반려 200")
    void rejectReturns200() throws Exception {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");
        RejectApplicationRequest request = new RejectApplicationRequest("사업자 정보 불일치");

        mockMvc.perform(post("/api/admin/seller/applications/{id}/reject", app.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이미 APPROVED 인 신청 승인 시도 → 400")
    void approveAlreadyApprovedReturns400() throws Exception {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");
        mockMvc.perform(post("/api/admin/seller/applications/{id}/approve", app.getId())
                .with(csrf()));

        mockMvc.perform(post("/api/admin/seller/applications/{id}/approve", app.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}