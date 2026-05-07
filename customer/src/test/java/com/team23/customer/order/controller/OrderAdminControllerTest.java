package com.team23.customer.order.controller;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderItem;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderAdminControllerTest {
    @Autowired
    MockMvc mockMvc;
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
}