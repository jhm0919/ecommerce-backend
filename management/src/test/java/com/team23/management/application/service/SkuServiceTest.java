package com.team23.management.application.service;

import com.team23.management.api.dto.SkuAddResponse;
import com.team23.management.application.command.SkuAddCommand;
import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.stock.Stock;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkuServiceTest {
    @Autowired
    SkuService skuService;
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
    @DisplayName("정상 — SKU + Stock 한 트랜잭션 생성")
    void addSkuSuccess() {
        // given
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        SkuAddCommand command = new SkuAddCommand(
                product.getId(), 1L,
                List.of(new SkuOptionInput("색상", "blue"), new SkuOptionInput("사이즈", "L")),
                1000, 50
        );

        // when
        SkuAddResponse response = skuService.addSku(command);

        // then
        assertThat(response.skuId()).isNotNull();
        assertThat(response.productId()).isEqualTo(product.getId());

        // DB 직접 검증
        Sku savedSku = skuRepository.findById(response.skuId()).orElseThrow();
        assertThat(savedSku.getAdditionalPrice()).isEqualTo(1000);

        Stock savedStock = stockRepository.findBySkuId(savedSku.getId());
        assertThat(savedStock.getQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("기존 SKU 와 옵션 조합 중복 → IllegalArgumentException")
    void addSkuDuplicateThrows() {
        // given
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        SkuAddCommand command1 = new SkuAddCommand(
                product.getId(), 1L,
                List.of(new SkuOptionInput("색상", "blue")
                ),
                1000, 50
        );

        SkuAddResponse response = skuService.addSku(command1);

        // 2차 — 같은 옵션 조합 시도
        SkuAddCommand command2 = new SkuAddCommand(
                product.getId(), 1L,
                List.of(
                        new SkuOptionInput("색상", "blue")
                ),
                2000, 30
        );

        // when
        // then
        assertThatThrownBy(() ->
                skuService.addSku(command2)).isInstanceOf(IllegalArgumentException.class);
    }
}