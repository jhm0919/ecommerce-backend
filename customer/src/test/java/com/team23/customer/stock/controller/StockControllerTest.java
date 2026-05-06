package com.team23.customer.stock.controller;

import com.team23.customer.stock.domain.ReceiveHistory;
import com.team23.customer.stock.repository.ReceiveHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class StockControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ReceiveHistoryRepository receiveHistoryRepository;

    @AfterEach
    void cleanUp() {
        receiveHistoryRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/seller/stocks/receive-history — 전체 조회")
    void historyAllReturns200() throws Exception {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));
        receiveHistoryRepository.save(ReceiveHistory.of(2L, 2L, 50,  150));

        // when & then
        mockMvc.perform(get("/api/seller/stocks/receive-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].receivedQuantity").exists())
                .andExpect(jsonPath("$.content[0].stockAfter").exists());
    }

    @Test
    @DisplayName("SKU 필터")
    void historyBySkuIdReturns200() throws Exception {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));
        receiveHistoryRepository.save(ReceiveHistory.of(2L, 2L, 50,  150));

        // when & then
        mockMvc.perform(get("/api/seller/stocks/receive-history")
                        .param("skuId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].skuId").value(1));
    }

    @Test
    @DisplayName("기간 필터")
    void historyByDateRangeReturns200() throws Exception {
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));

        mockMvc.perform(get("/api/seller/stocks/receive-history")
                        .param("from", LocalDate.now().toString())
                        .param("to",   LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("결과 없는 필터 → totalElements 0")
    void historyNoResultReturns200() throws Exception {
        mockMvc.perform(get("/api/seller/stocks/receive-history")
                        .param("skuId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }
}