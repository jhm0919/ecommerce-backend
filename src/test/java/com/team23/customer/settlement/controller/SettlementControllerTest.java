package com.team23.customer.settlement.controller;

import com.team23.customer.category.domain.Category;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderAdmin;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.repository.OrderAdminRepository;
import com.team23.customer.product.domain.*;
import com.team23.customer.category.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.settlement.dto.SettlementConfirmRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SettlementControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrderAdminRepository orderAdminRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ObjectMapper objectMapper;


    private Product testProduct;
    private Sku testSku;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(
                Category.create("신발", "shoe-settle-ctrl"));
        Product product = Product.register(
                "운동화",
                BigDecimal.valueOf(10000),
                "설명", "url", category);
        productRepository.save(product);

        List<SkuOption> options = List.of(new SkuOption("색상", "blue"));
        product.addSku(options, 100);
        Product saved = productRepository.saveAndFlush(product);
        this.testProduct = saved;
        this.testSku = saved.getSkuses().get(0);
    }

    private void createConfirmedOrder() {
        OrderItem item = OrderItem.of(testProduct, testSku, 1);
        Order order = Order.createForMember(1L, List.of(item));
        orderAdminRepository.save(order);
        OrderAdmin.confirm(order);
        orderAdminRepository.saveAndFlush(order);
    }

    @Test
    @DisplayName("GET /api/seller/settlements — 200")
    void getSettlementsReturns200() throws Exception {
        createConfirmedOrder();

        mockMvc.perform(get("/api/seller/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSalesAmount").isNumber())
                .andExpect(jsonPath("$.data.totalFee").isNumber())
                .andExpect(jsonPath("$.data.totalSettlementAmount").isNumber())
                .andExpect(jsonPath("$.data.feeRate").value(3.5))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].orderId").exists())
                .andExpect(jsonPath("$.data.items[0].orderNumber").isNotEmpty());
    }

    @Test
    @DisplayName("기간 필터 — 200")
    void getSettlementsWithDateFilterReturns200() throws Exception {
        createConfirmedOrder();

        mockMvc.perform(get("/api/seller/settlements")
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("CONFIRMED 없을 때 — 빈 items")
    void getSettlementsNoConfirmedReturnsEmpty() throws Exception {
        // PENDING 주문만 존재
        OrderItem item = OrderItem.of(testProduct, testSku, 1);
        Order order = Order.createForMember(1L, List.of(item));
        orderAdminRepository.saveAndFlush(order);

        mockMvc.perform(get("/api/seller/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.totalSalesAmount").value(0));
    }

    @Test
    @DisplayName("PATCH /api/seller/settlements/confirm — 정상 확정 200")
    void confirmSettlementReturns200() throws Exception {
        createConfirmedOrder();

        SettlementConfirmRequest request = new SettlementConfirmRequest(YearMonth.now());

        mockMvc.perform(patch("/api/seller/settlements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confirmedCount").value(1))
                .andExpect(jsonPath("$.data.settledMonth").isNotEmpty())
                .andExpect(jsonPath("$.data.totalSettlementAmount").isNumber());
    }

    @Test
    @DisplayName("정산 대상 없음 → 400")
    void confirmSettlementNoTargetReturns400() throws Exception {
        SettlementConfirmRequest request =
                new SettlementConfirmRequest(YearMonth.now());

        mockMvc.perform(patch("/api/seller/settlements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("settledMonth 누락 → 400")
    void confirmSettlementMissingMonthReturns400() throws Exception {
        mockMvc.perform(patch("/api/seller/settlements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
