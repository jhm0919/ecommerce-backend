package com.team23.management.application.service;

import com.team23.management.application.command.ProductRegisterCommand;
import com.team23.management.application.command.SkuCommand;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
//@Transactional
class ProductRegisterServiceTest {

    @Autowired
    ProductRegisterService productRegisterService;
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
    @DisplayName("상품 등록 시 Product / SKU / Stock 이 모두 생성된다")
    void registerValidInputCreatesAll() {
        // given
        ProductRegisterCommand command = new ProductRegisterCommand(
                "면 티셔츠", Category.FASHION, 19900, "면", 1L,
                List.of(
                        new SkuCommand(
                                List.of(
                                        new SkuOptionInput("색상", "white"),
                                        new SkuOptionInput("사이즈", "M")
                                ),
                                0,
                                100
                        )
                )
        );

        // when
        Long productId = productRegisterService.register(command);

        // then
        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(skuRepository.count()).isPositive();
        assertThat(stockRepository.count()).isPositive();
        // SKU / Stock 도 검증
    }

    @Test
    @DisplayName("SKU 생성 실패 시 Product 도 롤백된다")
    void registerSkuFailsRollsBackProduct() {
        // given — 잘못된 SKU (옵션 없음)
        ProductRegisterCommand command = new ProductRegisterCommand(
                "면 티셔츠", Category.FASHION, 19900, "면", 1L,
                List.of(
                        new SkuCommand(
                                List.of(),  // ← 옵션 0개 — Sku.create 에서 예외
                                0,
                                100
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> productRegisterService.register(command))
                .isInstanceOf(IllegalArgumentException.class);

        // 그리고 Product 가 *저장 안 됨* 확인
        assertThat(productRepository.count()).isZero();
    }
}