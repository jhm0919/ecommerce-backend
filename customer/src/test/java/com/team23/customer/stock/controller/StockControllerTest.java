package com.team23.customer.stock.controller;

import com.team23.customer.product.domain.*;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import com.team23.customer.stock.domain.ReceiveHistory;
import com.team23.customer.stock.dto.ReceiveAdjustRequest;
import com.team23.customer.stock.repository.ReceiveHistoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    private Long skuId;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(
                Category.create("바지", "pants"));
        Product product = Product.register(
                "청바지",
                new Money(BigDecimal.valueOf(10000), "KRW"),
                "설명", "url", category);
        productRepository.save(product);

        List<SkuOption> options = List.of(new SkuOption("색상", "blue"));
        product.addSku(options, 100);
        Product saved = productRepository.saveAndFlush(product);

        this.skuId = saved.getSkus().get(0).getId();
    }

    @AfterEach
    void cleanUp() {
        receiveHistoryRepository.deleteAll();   // 1. 이력 먼저
        purchaseOrderRepository.deleteAll();    // 2. 발주
        productRepository.deleteAll();          // 3. 상품 (cascade → sku, sku_options)
        categoryRepository.deleteAll();         // 4. 카테고리
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

    @Test
    @DisplayName("PATCH /api/seller/stocks/receive/{id}/cancel — 정상")
    @Transactional
    void cancelSuccessReturns200() throws Exception {
        // given
        PurchaseOrder po = purchaseOrderRepository.save(
                PurchaseOrder.create(skuId, 50, "공급사", null,
                        LocalDate.now().plusDays(7)));
        po.receive();
        purchaseOrderRepository.save(po);

        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, po.getId(), 50, 150));

        // when & then
        mockMvc.perform(patch("/api/seller/stocks/receive/" + history.getId() + "/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiveHistoryId").value(history.getId()))
                .andExpect(jsonPath("$.purchaseOrderStatus").value("REQUESTED"))
                .andExpect(jsonPath("$.cancelledQuantity").value(50));
    }

    @Test
    @DisplayName("없는 이력 ID → 404")
    void cancelNotFoundReturns404() throws Exception {
        mockMvc.perform(patch("/api/seller/stocks/receive/99999/cancel")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 취소된 이력 → 400")
    void cancelAlreadyCancelledReturns400() throws Exception {
        // given — 취소된 이력 만들기
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 50, 150));
        history.cancel();
        receiveHistoryRepository.save(history);

        // when & then
        mockMvc.perform(patch("/api/seller/stocks/receive/" + history.getId() + "/cancel")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adjustSuccessReturns200() throws Exception {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 100, 200));

        ReceiveAdjustRequest request = new ReceiveAdjustRequest(80, "수량 상이");

        mockMvc.perform(patch("/api/seller/stocks/receive/" + history.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalQuantity").value(100))
                .andExpect(jsonPath("$.adjustedQuantity").value(80))
                .andExpect(jsonPath("$.reason").value("수량 상이"));
    }

    @Test
    void adjustNotFoundReturns404() throws Exception {
        ReceiveAdjustRequest request = new ReceiveAdjustRequest(80, "수량 상이");

        mockMvc.perform(patch("/api/seller/stocks/receive/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adjustCancelledHistoryReturns400() throws Exception {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 100, 200));
        history.cancel();
        receiveHistoryRepository.saveAndFlush(history);

        ReceiveAdjustRequest request = new ReceiveAdjustRequest(80, "수량 상이");

        mockMvc.perform(patch("/api/seller/stocks/receive/" + history.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adjustZeroQuantityReturns400() throws Exception {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 100, 200));

        ReceiveAdjustRequest request = new ReceiveAdjustRequest(0, "수량 상이");

        mockMvc.perform(patch("/api/seller/stocks/receive/" + history.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adjustBlankReasonReturns400() throws Exception {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 100, 200));

        ReceiveAdjustRequest request = new ReceiveAdjustRequest(80, "");

        mockMvc.perform(patch("/api/seller/stocks/receive/" + history.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}