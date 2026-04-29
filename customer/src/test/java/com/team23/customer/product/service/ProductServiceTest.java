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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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
        @DisplayName("categoryId가 null이면 전체 조회")
        void allProductsWhenNoCategoryFilter() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> mockPage = new PageImpl<>(List.of(createProduct(10)));

            given(productRepository.findVisibleProducts(eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<Product> result = productService.findVisibleProducts(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProducts(ProductStatus.DISCONTINUED, pageable);
            verify(productRepository, never())
                    .findVisibleProductsByCategory(any(), any(), any());
        }

        @Test
        @DisplayName("categoryId가 있으면 카테고리별 조회")
        void filterByCategoryId() {
            Pageable pageable = PageRequest.of(0, 20);
            Long categoryId = 1L;
            Page<Product> mockPage = new PageImpl<>(List.of(createProduct(10)));

            given(productRepository.findVisibleProductsByCategory(
                    eq(categoryId), eq(ProductStatus.DISCONTINUED), eq(pageable)))
                    .willReturn(mockPage);

            Page<Product> result = productService.findVisibleProducts(categoryId, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findVisibleProductsByCategory(
                    categoryId, ProductStatus.DISCONTINUED, pageable);
            verify(productRepository, never()).findVisibleProducts(any(), any());
        }
    }

    @Nested
    @DisplayName("상품 상세 조회 (findById)")
    class FindById {

        @Test
        @DisplayName("ACTIVE 상품을 조회할 수 있다")
        void findActiveProduct() {
            Product product = createProduct(10);  // ACTIVE

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result).isEqualTo(product);
        }

        @Test
        @DisplayName("SOLD_OUT 상품도 조회 가능 (사용자에게 노출됨)")
        void findSoldOutProduct() {
            Product product = createProduct(0);  // SOLD_OUT

            given(productRepository.findByIdWithCategory(1L))
                    .willReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 NotFound로 응답")
        void rejectDiscontinuedAsNotFound() {
            Product product = createProduct(10);
            product.discontinue();  // DISCONTINUED

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