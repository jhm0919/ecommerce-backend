package com.team23.management.api;

import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.sku.SkuOptionInput;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SkuControllerTest {
    @Autowired
    MockMvc mockMvc;
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
    @DisplayName("POST /api/seller/products/{productId}/skus - 정상")
    void addSku_success_returns201() throws Exception {
        // given
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        String json = """
            {
              "sellerId": 1,
              "options": [
                {"name": "색상", "value": "blue"},
                {"name": "사이즈", "value": "L"}
              ],
              "additionalPrice": 1000,
              "initialStock": 50
            }
            """;

        // when & then
        mockMvc.perform(post("/api/seller/products/" + product.getId() + "/skus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skuId").exists())
                .andExpect(jsonPath("$.productId").value(product.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 productId → 404")
    void addSku_productNotFound_returns404() throws Exception {
        String json = """
        {
          "sellerId": 1,
          "options": [{"name": "색상", "value": "blue"}],
          "additionalPrice": 0,
          "initialStock": 10
        }
        """;

        mockMvc.perform(post("/api/seller/products/99999/skus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("sellerId 누락 → 400 (Bean Validation)")
    void addSku_missingSellerId_returns400() throws Exception {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        String json = """
        {
          "options": [{"name": "색상", "value": "blue"}],
          "additionalPrice": 0,
          "initialStock": 10
        }
        """;

        mockMvc.perform(post("/api/seller/products/" + product.getId() + "/skus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("옵션 조합 중복 → 400")
    void addSku_duplicateOptions_returns400() throws Exception {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        // 1차 등록 — 직접 도메인으로
        Sku existing = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 0);
        skuRepository.save(existing);

        // 2차 시도 — 같은 조합
        String json = """
        {
          "sellerId": 1,
          "options": [{"name": "색상", "value": "blue"}],
          "additionalPrice": 1000,
          "initialStock": 50
        }
        """;

        mockMvc.perform(post("/api/seller/products/" + product.getId() + "/skus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/seller/products/{pId}/skus/{sId} - 정상")
    void updateSuccessReturns200() throws Exception {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        Sku sku = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 1000);
        skuRepository.save(sku);

        String json = """
        {
          "sellerId": 1,
          "additionalPrice": 2500
        }
        """;

        mockMvc.perform(patch("/api/seller/products/" + product.getId() + "/skus/" + sku.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuId").value(sku.getId()))
                .andExpect(jsonPath("$.additionalPrice").value(2500));
    }

    @Test
    @DisplayName("없는 skuId → 404 또는 400")
    void updateSkuNotFound() throws Exception {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        Sku sku = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 1000);
        skuRepository.save(sku);

        String json = """
        {
          "sellerId": 1,
          "additionalPrice": 2500
        }
        """;

        mockMvc.perform(patch("/api/seller/products/" + product.getId() + "/skus/99999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}