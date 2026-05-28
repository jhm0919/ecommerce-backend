package com.team23.customer.product.service;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.domain.SkuOption;
import com.team23.customer.product.dto.ProductSummaryProjection;
import com.team23.customer.product.dto.ProductSummaryResponse;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;

    @InjectMocks private ProductService productService;

    private Category createCategory() {
        return Category.create("남성 상의", "men-tops");
    }

    private Product createProduct() {  // ★ stock 매개변수 제거
        return Product.register(
                "베이직 티셔츠",
                Money.krw(29900),
                "100% 면 소재",
                "https://example.com/image.jpg",
                createCategory()
        );
    }

    @Nested
    @DisplayName("상품 목록 조회 (findVisibleProducts)")
    class FindVisibleProducts {

        @Test
        @DisplayName("필터 없이 전체 조회")
        void allProducts() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ProductSummaryProjection> mockPage = new PageImpl<>(List.of(createSummary()));

            given(productRepository.findVisibleProductSummaries(
                    eq(null), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<ProductSummaryResponse> result = productService.findVisibleProducts(null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProductSummaries(
                    null, null, ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("카테고리 필터")
        void filterByCategoryId() {
            Pageable pageable = PageRequest.of(0, 20);
            Long categoryId = 1L;

            given(productRepository.findVisibleProductSummaries(
                    eq(categoryId), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(createSummary())));

            Page<ProductSummaryResponse> result = productService.findVisibleProducts(categoryId, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProductSummaries(
                    categoryId, null, ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("검색어로 조회")
        void searchByKeyword() {
            Pageable pageable = PageRequest.of(0, 20);

            given(productRepository.findVisibleProductSummaries(
                    eq(null), eq("티셔츠"), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(createSummary())));

            Page<ProductSummaryResponse> result = productService.findVisibleProducts(null, "티셔츠", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProductSummaries(
                    null, "티셔츠", ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("카테고리 + 검색어 조합")
        void filterByCategoryAndKeyword() {
            Pageable pageable = PageRequest.of(0, 20);
            Long categoryId = 1L;

            given(productRepository.findVisibleProductSummaries(
                    eq(categoryId), eq("티셔츠"), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(createSummary())));

            Page<ProductSummaryResponse> result = productService.findVisibleProducts(categoryId, "티셔츠", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProductSummaries(
                    categoryId, "티셔츠", ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("검색어가 trim된다")
        void trimKeyword() {
            Pageable pageable = PageRequest.of(0, 20);

            given(productRepository.findVisibleProductSummaries(
                    eq(null), eq("티셔츠"), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of()));

            productService.findVisibleProducts(null, "  티셔츠  ", pageable);

            verify(productRepository).findVisibleProductSummaries(
                    null, "티셔츠", ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("빈 검색어는 null로 처리")
        void emptyKeywordBecomesNull() {
            Pageable pageable = PageRequest.of(0, 20);

            given(productRepository.findVisibleProductSummaries(
                    eq(null), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of()));

            productService.findVisibleProducts(null, "   ", pageable);

            verify(productRepository).findVisibleProductSummaries(
                    null, null, ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("createdAt 정렬은 허용한다")
        void allowCreatedAtSort() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

            given(productRepository.findVisibleProductSummaries(
                    eq(null), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(createSummary())));

            Page<ProductSummaryResponse> result = productService.findVisibleProducts(null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProductSummaries(
                    null, null, ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("허용되지 않은 정렬 필드는 거부한다")
        void rejectUnsupportedSortField() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));

            assertThatThrownBy(() -> productService.findVisibleProducts(null, null, pageable))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported product sort field");
        }

        @Test
        @DisplayName("재고 합계가 int 범위를 초과하면 최대값으로 응답")
        void totalStockOverflowUsesMaxInteger() {
            ProductSummaryResponse response = ProductSummaryResponse.from(
                    createSummaryWithTotalStock((long) Integer.MAX_VALUE + 1)
            );

            assertThat(response.totalStock()).isEqualTo(Integer.MAX_VALUE);
            assertThat(response.inStock()).isTrue();
        }

        @Test
        @DisplayName("재고 합계가 null이면 0으로 응답")
        void totalStockNullBecomesZero() {
            ProductSummaryResponse response = ProductSummaryResponse.from(
                    createSummaryWithTotalStock(null)
            );

            assertThat(response.totalStock()).isZero();
            assertThat(response.inStock()).isFalse();
        }
    }

    @Nested
    @DisplayName("상품 상세 조회 (findById)")
    class FindById {

        @Test
        @DisplayName("ACTIVE 상품을 조회할 수 있다")
        void findActiveProduct() {
            Product product = createProduct();

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result).isEqualTo(product);
        }

        @Test
        @DisplayName("SOLD_OUT 상품도 조회 가능 (사용자에게 노출됨)")
        void findSoldOutProduct() {
            Product product = createProduct();
            setId(product, 1L);
            SKU sku = product.addSku(List.of(new SkuOption("색상", "검정")), 1);
            setId(sku, 100L);
            product.decreaseSkuStock(100L, 1);  // 모든 SKU 재고 0 → SOLD_OUT
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 NotFound로 응답")
        void rejectDiscontinuedAsNotFound() {
            Product product = createProduct();
            product.discontinue();

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.findById(1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 ID는 ProductNotFoundException")
        void rejectUnknownId() {
            given(productRepository.findByIdWithCategory(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(999L))
                    .isInstanceOf(ProductNotFoundException.class);
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

    private ProductSummaryProjection createSummary() {
        return createSummaryWithTotalStock(10L);
    }

    private ProductSummaryProjection createSummaryWithTotalStock(Long totalStock) {
        return new ProductSummaryProjection(
                1L,
                "베이직 티셔츠",
                BigDecimal.valueOf(29900),
                "KRW",
                "https://example.com/image.jpg",
                "남성 상의",
                ProductStatus.ACTIVE,
                totalStock
        );
    }
}
