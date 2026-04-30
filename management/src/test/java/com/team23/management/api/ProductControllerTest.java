package com.team23.management.api;

import com.team23.management.api.dto.ProductRegisterRequest;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.product.Category;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;
    @Autowired
    SkuRepository skuRepository;
    @Autowired
    StockRepository stockRepository;

    @AfterEach
    void cleanUp() {
        stockRepository.deleteAll();
        skuRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/seller/products - 상품 등록 성공 시 201 반환")
    void registerValidRequestReturns201() throws Exception {
        // given
        ProductRegisterRequest request = new ProductRegisterRequest(
                "면 티셔츠", Category.FASHION, 19900, "면", 1L,
                List.of(
                        new ProductRegisterRequest.SkuRequest(
                                List.of(
                                        new SkuOptionInput("색상", "white"),
                                        new SkuOptionInput("사이즈", "M")
                                ),
                                0,
                                100
                        )
                )
        );

        // when & then
        mockMvc.perform(post("/api/seller/products")
//                        .with(csrf())   // ← CSRF 토큰 자동 생성
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").exists());
    }

    @Test
    @DisplayName("GET /api/seller/products - 검색 조건 없이 호출")
    void searchNoConditionReturnsAll() throws Exception {
        // given
        Product p1 = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        Product p2 = Product.create("청바지", Category.FASHION, 30000, "d", 1L);
        productRepository.save(p1);
        productRepository.save(p2);

        // when & then
        mockMvc.perform(get("/api/seller/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("상품명으로 검색")
    void searchByName() throws Exception {
        // given
        Product p1 = Product.create("면 티셔츠", Category.FASHION, 10000, "d", 1L);
        Product p2 = Product.create("청바지", Category.FASHION, 30000, "d", 1L);
        productRepository.save(p1);
        productRepository.save(p2);

        // when & then
        mockMvc.perform(get("/api/seller/products")
                        .param("name", "면"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("면 티셔츠"));
    }
}