package com.team23.customer.product.service;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ProductAdminService productAdminService;

    private Category createCategory() {
        return Category.create("남성 상의", "men-tops");
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
                    10,
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
            assertThat(result.getStock()).isEqualTo(10);
            assertThat(result.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            ProductCreateRequest request = new ProductCreateRequest(
                    "베이직 티셔츠",
                    new BigDecimal("29900"),
                    "KRW",
                    10,
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
            Product product = Product.register(
                    "원래 이름",
                    Money.krw(29900),
                    10,
                    "설명",
                    "img",
                    createCategory()
            );

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
            Product product = Product.register(
                    "이름", Money.krw(29900), 10, "설명", "img", createCategory()
            );

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

    @Nested
    @DisplayName("재고 증가 (increaseStock)")
    class IncreaseStock {

        @Test
        @DisplayName("재고를 증가시킬 수 있다")
        void increaseStockNormal() {
            Product product = Product.register(
                    "이름", Money.krw(29900), 10, "설명", "img", createCategory()
            );

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            Product result = productAdminService.increaseStock(1L, 5);

            assertThat(result.getStock()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("상품 단종 (discontinue)")
    class Discontinue {

        @Test
        @DisplayName("상품을 단종 처리할 수 있다")
        void discontinueNormal() {
            Product product = Product.register(
                    "이름", Money.krw(29900), 10, "설명", "img", createCategory()
            );

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productAdminService.discontinue(1L);

            assertThat(product.getStatus().name()).isEqualTo("DISCONTINUED");
        }
    }
}