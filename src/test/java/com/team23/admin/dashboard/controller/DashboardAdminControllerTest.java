package com.team23.admin.dashboard.controller;

import com.team23.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardAdminControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/admin/dashboard/sales - 정상 조회 200")
    void getSales_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("unit", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.unit").value("DAILY"))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("unit 기본값 DAILY")
    void unitDefault_DAILY() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unit").value("DAILY"));
    }

    @Test
    @DisplayName("MONTHLY 단위 조회")
    void unitMonthly_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-03-31")
                        .param("unit", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unit").value("MONTHLY"));
    }

    @Test
    @DisplayName("startDate > endDate → 400")
    void invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-10")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기간 365일 초과 → 400")
    void rangeTooLong_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2027-06-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("필수 파라미터 누락 → 400")
    void missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 unit 값 → 400")
    void invalidUnit_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/sales")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("unit", "YEARLY"))
                .andExpect(status().isBadRequest());
    }

    // 결제 건수

    @Test
    @DisplayName("[payments] GET /api/admin/dashboard/payments - 정상 조회 200")
    void getPayments_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/payments")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("unit", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.unit").value("DAILY"))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("[payments] unit 기본값 DAILY")
    void payments_unitDefault_DAILY() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/payments")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unit").value("DAILY"));
    }

    @Test
    @DisplayName("[payments] MONTHLY 단위 조회")
    void payments_unitMonthly_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/payments")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-03-31")
                        .param("unit", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unit").value("MONTHLY"));
    }

    @Test
    @DisplayName("[payments] startDate > endDate → 400")
    void payments_invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/payments")
                        .param("startDate", "2026-01-10")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[payments] 잘못된 unit 값 → 400")
    void payments_invalidUnit_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/payments")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("unit", "YEARLY"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[refunds] GET /api/admin/dashboard/refunds - 정상 조회 200")
    void getRefunds_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/refunds")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("unit", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.unit").value("DAILY"))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("[refunds] MONTHLY 단위 조회")
    void refunds_unitMonthly_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/refunds")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-03-31")
                        .param("unit", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unit").value("MONTHLY"));
    }

    @Test
    @DisplayName("[refunds] startDate > endDate → 400")
    void refunds_invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/refunds")
                        .param("startDate", "2026-01-10")
                        .param("endDate", "2026-01-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[refunds] 잘못된 unit 값 → 400")
    void refunds_invalidUnit_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/refunds")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("unit", "YEARLY"))
                .andExpect(status().isBadRequest());
    }

}