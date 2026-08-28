package com.shop.product.service;

import com.shop.category.domain.Category;
import com.shop.category.repository.CategoryRepository;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;
import com.shop.product.domain.Product;
import com.shop.product.domain.ProductStatus;
import com.shop.product.domain.SkuOption;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.dto.ProductListResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("검색어가 trim된다")
    void trimKeyword() {
        //given
        Pageable pageable = PageRequest.of(0, 20);

        Product product = mock(Product.class);
        Category category = mock(Category.class);

        given(product.getCategory()).willReturn(category);
        given(category.getName()).willReturn("상의");

        given(productRepository.findProductList(
                null, "티셔츠", ProductStatus.DISCONTINUED, pageable)
        ).willReturn(new PageImpl<>(List.of(product)));

        //when
        Page<ProductListResponse> response = productService.findProductList(
                null, "  티셔츠  ", pageable
        );

        //then
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findProductList(
                null,
                "티셔츠",
                ProductStatus.DISCONTINUED,
                pageable
        );
    }

    @Test
    @DisplayName("빈 검색어는 null로 처리")
    void emptyKeywordBecomesNull() {
        //given
        Pageable pageable = PageRequest.of(0, 20);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);
        Category category = mock(Category.class);

        given(product1.getCategory()).willReturn(category);
        given(product2.getCategory()).willReturn(category);
        given(category.getName()).willReturn("상의");

        given(productRepository.findProductList(
                null, null, ProductStatus.DISCONTINUED, pageable)
        ).willReturn(new PageImpl<>(List.of(product1, product2)));

        //when
        Page<ProductListResponse> response = productService.findProductList(
                null, "   ", pageable
        );

        //then
        assertThat(response.getContent()).hasSize(2);
        verify(productRepository).findProductList(
                null,
                null,
                ProductStatus.DISCONTINUED,
                pageable
        );
    }

    @Test
    @DisplayName("createdAt 정렬은 허용한다")
    void allowCreatedAtSort() {
        //given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);
        Category category = mock(Category.class);

        given(product1.getCategory()).willReturn(category);
        given(product2.getCategory()).willReturn(category);
        given(category.getName()).willReturn("상의");

        given(productRepository.findProductList(
                null, null, ProductStatus.DISCONTINUED, pageable)
        ).willReturn(new PageImpl<>(List.of(product1, product2)));

        //when
        Page<ProductListResponse> result = productService.findProductList(null, null, pageable);

        //then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("허용되지 않은 정렬 필드는 거부한다")
    void rejectUnsupportedSortField() {
        //given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "name"));

        //when & then
        assertThatThrownBy(() -> productService.findProductList(null, null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported product sort field");
    }

    @Test
    @DisplayName("DISCONTINUED 상품은 NotFound로 응답")
    void rejectDiscontinuedAsNotFound() {
        //given
        Long productId = 1L;
        Product product = mock(Product.class);

        given(productRepository.findByIdWithSkus(productId))
                .willReturn(Optional.of(product));
        given(product.isVisibleToCustomer()).willReturn(false);

        //when & then
        assertThatThrownBy(() -> productService.findById(productId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 ID는 ProductNotFoundException")
    void rejectUnknownId() {
        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}

