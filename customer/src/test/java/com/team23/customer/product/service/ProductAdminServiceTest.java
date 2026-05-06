package com.team23.customer.product.service;

import com.team23.customer.product.domain.*;
import com.team23.customer.product.dto.ProductCreateRequest;
import com.team23.customer.product.dto.ProductUpdateRequest;
import com.team23.customer.product.exception.CategoryNotFoundException;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;   // ★ 수정
import static org.mockito.Mockito.verify;  // ★ 수정

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ProductAdminService productAdminService;

    private Category createCategory() {
        return Category.create("남성 상의", "men-tops");
    }

    private Product createProduct() {
        return Product.register(
                "베이직 티셔츠",
                Money.krw(29900),
                "100% 면 소재",
                "https://example.com/image.jpg",
                createCategory()
        );
    }

    @Nested
    @DisplayName("상품 등록 (register)")
    class Register {

        @Test
        @DisplayName("정상적으로 상품을 등록할 수 있다")
        void registerNormal() {
            ProductCreateRequest request = new ProductCreateRequest(
                    "베이직 티셔츠",
                    new BigDecimal("29900"),
                    "KRW",
                    "100% 면 소재",        // ★ stock 제거
                    "https://example.com/image.jpg",
                    1L
            );

            Category category = createCategory();
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Product result = productAdminService.register(request);

            assertThat(result.getName()).isEqualTo("베이직 티셔츠");
            assertThat(result.getStatus().name()).isEqualTo("ACTIVE");  // ★ stock 대신 status
            assertThat(result.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            ProductCreateRequest request = new ProductCreateRequest(
                    "베이직 티셔츠",
                    new BigDecimal("29900"),
                    "KRW",
                    "설명",               // ★ stock 제거
                    "https://...",
                    999L
            );

            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productAdminService.register(request))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 수정 (update)")
    class Update {

        @Test
        @DisplayName("이름만 수정할 수 있다")
        void updateNameOnly() {
            Product product = createProduct();  // ★ stock 제거

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "새 이름", null, null, null, null, null
            );

            Product result = productAdminService.update(1L, request);

            assertThat(result.getName()).isEqualTo("새 이름");
        }

        @Test
        @DisplayName("price만 있고 currency 없으면 예외")
        void rejectPriceWithoutCurrency() {
            Product product = createProduct();  // ★ stock 제거

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            ProductUpdateRequest request = new ProductUpdateRequest(
                    null, new BigDecimal("39900"), null, null, null, null
            );

            assertThatThrownBy(() -> productAdminService.update(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("price and currency");
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            ProductUpdateRequest request = new ProductUpdateRequest(
                    "이름", null, null, null, null, null
            );

            assertThatThrownBy(() -> productAdminService.update(999L, request))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    // ★ IncreaseStock 섹션 제거 → increaseSkuStock으로 대체

    @Nested
    @DisplayName("SKU 재고 증가 (increaseSkuStock)")
    class IncreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 증가시킬 수 있다")
        void increaseSkuStockNormal() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.increaseSkuStock(1L, 100L, 5);

            assertThat(sku.getStock()).isEqualTo(15);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productAdminService.increaseSkuStock(999L, 100L, 5))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("SKU 재고 감소 (decreaseSkuStock)")
    class DecreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 감소시킬 수 있다")
        void decreaseSkuStockNormal() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);

            assertThat(sku.getStock()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("상품 단종 (discontinue)")
    class Discontinue {

        @Test
        @DisplayName("상품을 단종 처리할 수 있다")
        void discontinueNormal() {
            Product product = createProduct();  // ★ stock 제거

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.discontinue(1L);

            assertThat(product.getStatus().name()).isEqualTo("DISCONTINUED");
        }
    }

    @Nested
    @DisplayName("SKU 재고 감소 (이벤트 발행)")
    class DecreaseSkuStockEvent {

        @Test
        @DisplayName("재고 0이면 품절 이벤트 발행")
        void publishEventWhenSoldOut() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 3);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);  // 재고 3→0

            verify(eventPublisher).publishEvent(any(SkuSoldOutEvent.class));
        }

        @Test
        @DisplayName("재고 남으면 이벤트 발행 안 함")
        void noEventWhenStockRemains() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);  // 재고 10→7

            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // ─── 테스트 헬퍼 ───

    private static void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}