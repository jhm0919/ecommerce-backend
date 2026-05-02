package com.team23.management.application.service;

import com.team23.management.api.dto.response.SkuAddResponse;
import com.team23.management.api.dto.response.SkuUpdateResponse;
import com.team23.management.application.command.SkuAddCommand;
import com.team23.management.application.command.SkuUpdateCommand;
import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.stock.Stock;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("정상 — 변경 감지로 DB 반영")
    void updateSuccess() {
        // given
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        Sku sku = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 1000);
        skuRepository.save(sku);

        SkuUpdateCommand command = new SkuUpdateCommand(
                product.getId(), sku.getId(), 1L, 2500
        );

        // when
        SkuUpdateResponse response = skuService.update(command);

        // then
        assertThat(response.additionalPrice()).isEqualTo(2500);

        // DB 다시 조회 — 진짜 반영됐는가
        Sku updated = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(updated.getAdditionalPrice()).isEqualTo(2500);
    }

    @Test
    @DisplayName("정상 삭제 — SKU + Stock 함께 사라짐")
    void deleteSuccess() {
        // given - Product + SKU 2개 (마지막 SKU 차단 회피) + Stock
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        Sku sku1 = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 0);
        Sku sku2 = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "red")), 0);
        skuRepository.saveAll(List.of(sku1, sku2));

        Stock stock1 = Stock.create(sku1.getId(), 0);   // 재고 0 (삭제 가능)
        Stock stock2 = Stock.create(sku2.getId(), 50);
        stockRepository.saveAll(List.of(stock1, stock2));

        // when
        skuService.delete(product.getId(), sku1.getId(), 1L);

        // then - SKU 삭제 확인
        assertThat(skuRepository.findById(sku1.getId())).isEmpty();
        // Stock 도 삭제 확인
        // (Stock 의 findBySkuId 가 Optional 이라면)
        // assertThat(stockRepository.findBySkuId(sku1.getId())).isNull();
    }

    @Test
    @DisplayName("재고 > 0 → 예외")
    void deleteStockExistsThrows() {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        Sku sku1 = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 0);
        Sku sku2 = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "red")), 0);
        skuRepository.saveAll(List.of(sku1, sku2));

        stockRepository.save(Stock.create(sku1.getId(), 100));   // 재고 있음
        stockRepository.save(Stock.create(sku2.getId(), 0));

        assertThatThrownBy(() ->
                skuService.delete(product.getId(), sku1.getId(), 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고");
    }

    @Test
    @DisplayName("마지막 SKU → 예외")
    void deleteLastSkuThrows() {
        Product product = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(product);

        Sku sku = Sku.create(product.getId(),
                List.of(new SkuOptionInput("색상", "blue")), 0);
        skuRepository.save(sku);
        stockRepository.save(Stock.create(sku.getId(), 0));

        assertThatThrownBy(() ->
                skuService.delete(product.getId(), sku.getId(), 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("마지막");
    }

}