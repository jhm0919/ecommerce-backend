package com.team23.customer.product.service;

import com.team23.customer.product.domain.*;
import com.team23.customer.product.dto.ProductCreateRequest;
import com.team23.customer.product.dto.ProductDetailResponse;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
                    "100% 면 소재",
                    "https://example.com/image.jpg",
                    1L
            );

            Category category = createCategory();
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Product result = productAdminService.register(request);

            assertThat(result.getName()).isEqualTo("베이직 티셔츠");
            assertThat(result.getStatus().name()).isEqualTo("ACTIVE");
            assertThat(result.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            ProductCreateRequest request = new ProductCreateRequest(
                    "베이직 티셔츠",
                    new BigDecimal("29900"),
                    "KRW",
                    "설명",
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
            Product product = createProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            ProductDetailResponse result = productAdminService.update(1L,
                    new ProductUpdateRequest("새 이름", null, null, null, null, null));

            assertThat(result.name()).isEqualTo("새 이름");
        }

        @Test
        @DisplayName("price만 있고 currency 없으면 예외")
        void rejectPriceWithoutCurrency() {
            Product product = createProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> productAdminService.update(1L,
                    new ProductUpdateRequest(null, new BigDecimal("39900"), null, null, null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("price and currency");
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productAdminService.update(999L,
                    new ProductUpdateRequest("이름", null, null, null, null, null)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("SKU 재고 증가 (increaseSkuStock)")
    class IncreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 증가시킨다")
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
        @DisplayName("재고 증가 시 StockChangedEvent 발행")  // ★ 추가
        void publishStockChangedEventOnIncrease() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.increaseSkuStock(1L, 100L, 5);

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
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
        @DisplayName("SKU 재고를 감소시킨다")
        void decreaseSkuStockNormal() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);

            assertThat(sku.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고 감소 시 항상 StockChangedEvent 발행")  // ★ 추가
        void alwaysPublishStockChangedEvent() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);  // 재고 10→7

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("재고 0이어도 StockChangedEvent만 발행")
        void publishOnlyStockChangedEventWhenSoldOut() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 3);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);  // 재고 3→0

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("재고 남아도 StockChangedEvent는 발행")
        void publishStockChangedEventWhenStockRemains() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.decreaseSkuStock(1L, 100L, 3);  // 재고 10→7

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }
    }

    @Nested
    @DisplayName("SKU 추가 (addSku)")  // ★ 추가
    class AddSku {

        @Test
        @DisplayName("SKU 추가 시 SKU_CREATED 이벤트 발행")
        void publishSkuCreatedEvent() {
            // given
            Product product = createProduct();
            setId(product, 1L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            // ★★★ 이 부분이 핵심입니다 ★★★
            // productRepository.save()가 호출되면, 인자로 받은 product 객체를 그대로 반환하도록 설정합니다.
            // 이렇게 해야 savedProduct가 null이 되지 않습니다.
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> {
                        Product savedProduct = invocation.getArgument(0);
                        // 실제 DB처럼, 저장된 SKU에 ID를 부여하는 것을 흉내 냅니다.
                        if (!savedProduct.getSkus().isEmpty()) {
                            setId(savedProduct.getSkus().get(0), 200L); // 임의의 SKU ID 부여
                        }
                        return savedProduct;
                    });

            // when
            productAdminService.addSku(1L,
                    List.of(new SkuOption("색상", "검정")), 10);

            // then
            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }
    }

    @Nested
    @DisplayName("상품 단종 (discontinue)")
    class Discontinue {

        @Test
        @DisplayName("상품을 단종 처리할 수 있다")
        void discontinueNormal() {
            Product product = createProduct();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.discontinue(1L);

            assertThat(product.getStatus().name()).isEqualTo("DISCONTINUED");
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
