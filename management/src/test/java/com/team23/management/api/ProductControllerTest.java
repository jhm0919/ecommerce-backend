package com.team23.management.api;

import com.team23.management.api.dto.ProductRegisterRequest;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.product.Category;
import com.team23.management.domain.stock.Stock;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    @DisplayName("PATCH /api/seller/products/{id} - 정상 수정")
    void update_validRequest_returns200() throws Exception {
        // given - 상품 등록
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        String json = """
            {
              "sellerId": 1,
              "name": "새 이름",
              "basePrice": 25000
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/seller/products/" + product.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 이름"))
                .andExpect(jsonPath("$.basePrice").value(25000))
                .andExpect(jsonPath("$.category").value("FASHION"));   // 변경 안 한 필드 유지
    }

    @Test
    @DisplayName("GET /api/seller/products/{id} - 정상 조회")
    void getDetailNormalProductReturns200() throws Exception {
        // given
        Product product = Product.create("면 티셔츠", Category.FASHION, 10000, "면", 1L);
        productRepository.save(product);

        Sku sku = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "white")), 0);
        skuRepository.save(sku);
        stockRepository.save(Stock.create(sku.getId(), 100));

        // when & then
        mockMvc.perform(get("/api/seller/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("면 티셔츠"))
                .andExpect(jsonPath("$.skus").isArray())
                .andExpect(jsonPath("$.skus.length()").value(1))
                .andExpect(jsonPath("$.skus[0].stock").value(100))
                .andExpect(jsonPath("$.skus[0].options[0].name").value("색상"));
    }

    @Test
    @DisplayName("존재하지 않는 productId → 404")
    void getDetailNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/seller/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETED 상품 조회 → 404")
    void getDetailDeletedProductReturns404() throws Exception {
        // given
        Product product = Product.create("삭제됨", Category.FASHION, 10000, "d", 1L);
        product.delete();
        productRepository.save(product);

        // when & then
        mockMvc.perform(get("/api/seller/products/" + product.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/seller/products/{id} - 정상")
    void deleteSuccessReturns204() throws Exception {
        // given
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        // when & then
        mockMvc.perform(delete("/api/seller/products/" + product.getId())
                        .with(csrf())
                        .param("sellerId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 productId → 404")
    void deleteNotFoundReturns404() throws Exception {
        mockMvc.perform(delete("/api/seller/products/99999")
                        .with(csrf())
                        .param("sellerId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 sellerId → 400")
    void deleteWrongSellerReturns400() throws Exception {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        mockMvc.perform(delete("/api/seller/products/" + product.getId())
                        .with(csrf())
                        .param("sellerId", "999"))
                .andExpect(status().isBadRequest());
    }
}