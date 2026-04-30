package com.team23.management.application.service;

import com.team23.management.application.command.ProductRegisterCommand;
import com.team23.management.application.command.ProductUpdateCommand;
import com.team23.management.application.command.SkuCommand;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.product.Category;
import com.team23.management.exception.ProductNotFoundException;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
//@Transactional
class ProductServiceTest {

    @Autowired
    ProductService productService;
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
        Long productId = productService.register(command);

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
        assertThatThrownBy(() -> productService.register(command))
                .isInstanceOf(IllegalArgumentException.class);

        // 그리고 Product 가 *저장 안 됨* 확인
        assertThat(productRepository.count()).isZero();
    }

    @Test
    @DisplayName("검색 조건 없이 호출 시 모든 활성 상품 반환")
    void searchNoConditionReturnsAll() {
        //given
        Product p1 = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        Product p2 = Product.create("청바지", Category.FASHION, 30000, "d", 1L);
        Product p3 = Product.create("삭제됨", Category.FOOD, 5000, "d", 1L);
        p3.delete();

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        //when
        Page<Product> result = productService.search(null, null, PageRequest.of(0, 20));

        //then
        assertThat(result.getContent()).hasSize(2);

    }

    @Test
    @DisplayName("정상 수정 - 변경 감지 동작 확인")
    void updateDirtyChecking() {
        //given - 상품 저장
        Product p = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(p);
        Long savedId = p.getId();

        //when - update 호출
        productService.update(new ProductUpdateCommand(
                savedId, 1L, "새 이름", null, null, null
        ));

        //then - DB 에서 다시 조회 → 진짜로 바뀌었는가
        Product updated = productRepository.findById(savedId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("새 이름");

    }

    @Test
    @DisplayName("존재하지 않는 productId 조회")
    void updateNotExistsProductThrows() {
        //given - 상품 저장
        Product p = Product.create("티셔츠", Category.FASHION, 10000, "d", 1L);
        productRepository.save(p);
        Long savedId = p.getId();

        //when - update 호출


        //then - DB 에서 다시 조회 → 진짜로 바뀌었는가
        Product updated = productRepository.findById(savedId).orElseThrow();
        assertThatThrownBy(() -> productService.update(new ProductUpdateCommand(
                111L, 1L, "새 이름", null, null, null
        ))).isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("상품");

    }
}