package com.team23.customer.settlement.controller;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderAdmin;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SettlementControllerTest {

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
                Category.create("신발", "shoe-settle-ctrl"));
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
                .andExpect(jsonPath("$.totalSalesAmount").isNumber())
                .andExpect(jsonPath("$.totalFee").isNumber())
                .andExpect(jsonPath("$.totalSettlementAmount").isNumber())
                .andExpect(jsonPath("$.feeRate").value(3.5))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].orderId").exists())
                .andExpect(jsonPath("$.items[0].orderNumber").isNotEmpty());
    }

    @Test
    @DisplayName("기간 필터 — 200")
    void getSettlementsWithDateFilterReturns200() throws Exception {
        createConfirmedOrder();

        mockMvc.perform(get("/api/seller/settlements")
                        .param("from", LocalDate.now().toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
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
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalSalesAmount").value(0));
    }
}