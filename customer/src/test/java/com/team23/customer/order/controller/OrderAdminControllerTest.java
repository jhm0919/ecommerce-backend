package com.team23.customer.order.controller;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.dto.OrderAdminCancelRequest;
import com.team23.customer.order.dto.OrderAdminConfirmRequest;
import com.team23.customer.order.repository.OrderAdminRepository;
import com.team23.customer.product.domain.*;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderAdminControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OrderAdminRepository orderAdminRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;

    private Product testProduct;
    private SKU testSku;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(
                Category.create("신발", "shoe-ctrl"));
        Product product = Product.register(
                "운동화",
                new Money(BigDecimal.valueOf(10000), "KRW"),
                "설명", "url", category);
        productRepository.save(product);

        List<SkuOption> options = List.of(new SkuOption("색상", "blue"));
        product.addSku(options, 100);
        Product saved = productRepository.saveAndFlush(product);
        this.testProduct = saved;
        this.testSku = saved.getSkus().get(0);
    }

    @AfterEach
    void cleanUp() {
        orderAdminRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Order createPendingOrder() {
        OrderItem item = OrderItem.of(testProduct, testSku, 1);
        Order order = Order.createForMember(1L, List.of(item));
        return orderAdminRepository.saveAndFlush(order);
    }

    @Test
    @DisplayName("GET /api/seller/orders - 전체 조회 200")
    void searchAllReturns200() throws Exception {
        createPendingOrder();
        createPendingOrder();

        mockMvc.perform(get("/api/seller/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].itemCount").value(1));
    }

    @Test
    @DisplayName("상태 필터 - 200")
    void searchByStatusReturns200() throws Exception {
        createPendingOrder();

        mockMvc.perform(get("/api/seller/orders")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("기간 필터 - 200")
    void searchByDateRangeReturns200() throws Exception {
        createPendingOrder();

        mockMvc.perform(get("/api/seller/orders")
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("결과 없는 필터 - totalElements 0")
    void searchNoResultReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/seller/orders")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("PATCH /api/seller/orders/confirm — 정상 확정 200")
    void confirmOrdersReturns200() throws Exception {
        Order o1 = createPendingOrder();
        Order o2 = createPendingOrder();

        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of(o1.getId(), o2.getId()));

        mockMvc.perform(patch("/api/seller/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.confirmedOrderIds").isArray());
    }

    @Test
    @DisplayName("없는 orderId → 404")
    void confirmWithNotFoundIdReturns404() throws Exception {
        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of(99999L));

        mockMvc.perform(patch("/api/seller/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CANCELLED 주문 확정 → 400")
    void confirmCancelledOrderReturns400() throws Exception {
        Order cancelled = createPendingOrder();
        cancelled.cancel();
        orderAdminRepository.saveAndFlush(cancelled);

        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of(cancelled.getId()));

        mockMvc.perform(patch("/api/seller/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 목록 요청 → 400")
    void confirmEmptyListReturns400() throws Exception {
        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of());

        mockMvc.perform(patch("/api/seller/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/seller/orders/{orderId}/cancel — 정상 취소 200")
    void cancelOrderReturns200() throws Exception {
        Order order = createPendingOrder();

        OrderAdminCancelRequest request = new OrderAdminCancelRequest("재고 부족", "OUT_OF_STOCK");

        mockMvc.perform(post("/api/seller/orders/" + order.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelReason").value("재고 부족"));
    }

    @Test
    @DisplayName("없는 orderId → 404")
    void cancelNotFoundOrderReturns404() throws Exception {
        OrderAdminCancelRequest request = new OrderAdminCancelRequest("사유", "CODE");

        mockMvc.perform(post("/api/seller/orders/99999/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("취소 사유 누락 → 400")
    void cancelWithBlankReasonReturns400() throws Exception {
        Order order = createPendingOrder();

        OrderAdminCancelRequest request = new OrderAdminCancelRequest("", "CODE");

        mockMvc.perform(post("/api/seller/orders/" + order.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 취소된 주문 → 400")
    void cancelAlreadyCancelledOrderReturns400() throws Exception {
        Order order = createPendingOrder();
        OrderAdminCancelRequest request = new OrderAdminCancelRequest("사유", "CODE");

        // 1차 취소
        mockMvc.perform(post("/api/seller/orders/" + order.getId() + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()));

        // 2차 취소 시도
        mockMvc.perform(post("/api/seller/orders/" + order.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}