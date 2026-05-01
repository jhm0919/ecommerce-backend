package com.team23.customer.product.service;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
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

    private Product createProduct(int stock) {
        return Product.register(
                "베이직 티셔츠",
                Money.krw(29900),
                stock,
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
            Page<Product> mockPage = new PageImpl<>(List.of(createProduct(10)));

            given(productRepository.findVisibleProducts(
                    eq(null), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<Product> result = productService.findVisibleProducts(null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProducts(
                    null, null, ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("카테고리 필터")
        void filterByCategoryId() {
            Pageable pageable = PageRequest.of(0, 20);
            Long categoryId = 1L;
            Page<Product> mockPage = new PageImpl<>(List.of(createProduct(10)));

            given(productRepository.findVisibleProducts(
                    eq(categoryId), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<Product> result = productService.findVisibleProducts(categoryId, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProducts(
                    categoryId, null, ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("검색어로 조회")
        void searchByKeyword() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> mockPage = new PageImpl<>(List.of(createProduct(10)));

            given(productRepository.findVisibleProducts(
                    eq(null), eq("티셔츠"), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<Product> result = productService.findVisibleProducts(null, "티셔츠", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProducts(
                    null, "티셔츠", ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("카테고리 + 검색어 조합")
        void filterByCategoryAndKeyword() {
            Pageable pageable = PageRequest.of(0, 20);
            Long categoryId = 1L;
            Page<Product> mockPage = new PageImpl<>(List.of(createProduct(10)));

            given(productRepository.findVisibleProducts(
                    eq(categoryId), eq("티셔츠"), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<Product> result = productService.findVisibleProducts(categoryId, "티셔츠", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProducts(
                    categoryId, "티셔츠", ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("검색어가 trim된다")
        void trimKeyword() {
            Pageable pageable = PageRequest.of(0, 20);

            given(productRepository.findVisibleProducts(
                    eq(null), eq("티셔츠"), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of()));

            productService.findVisibleProducts(null, "  티셔츠  ", pageable);

            verify(productRepository).findVisibleProducts(
                    null, "티셔츠", ProductStatus.DISCONTINUED, pageable);
        }

        @Test
        @DisplayName("빈 검색어는 null로 처리")
        void emptyKeywordBecomesNull() {
            Pageable pageable = PageRequest.of(0, 20);

            given(productRepository.findVisibleProducts(
                    eq(null), eq(null), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of()));

            productService.findVisibleProducts(null, "   ", pageable);

            verify(productRepository).findVisibleProducts(
                    null, null, ProductStatus.DISCONTINUED, pageable);
        }
    }

    @Nested
    @DisplayName("상품 상세 조회 (findById)")
    class FindById {

        @Test
        @DisplayName("ACTIVE 상품을 조회할 수 있다")
        void findActiveProduct() {
            Product product = createProduct(10);

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result).isEqualTo(product);
        }

        @Test
        @DisplayName("SOLD_OUT 상품도 조회 가능 (사용자에게 노출됨)")
        void findSoldOutProduct() {
            Product product = createProduct(0);

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 NotFound로 응답")
        void rejectDiscontinuedAsNotFound() {
            Product product = createProduct(10);
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
}