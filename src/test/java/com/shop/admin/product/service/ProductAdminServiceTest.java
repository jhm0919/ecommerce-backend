package com.shop.admin.product.service;

import com.shop.admin.product.dto.ProductAdminCreateRequest;
import com.shop.admin.product.dto.ProductAdminUpdateRequest;
import com.shop.admin.product.dto.SkuAdminAddRequest;
import com.shop.admin.product.dto.SkuAdminResponse;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.repository.CategoryRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {

    @InjectMocks
    private ProductAdminService productAdminService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CartRepository cartRepository;

    private final Long productId = 1L;
    private final Long categoryId = 1L;


    private Category createCategory() {
        return Category.create("남성 상의", "men-tops");
    }

    private Product createProduct() {
        return Product.register(
                "베이직 티셔츠",
                29900,
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
            Category category = createCategory();

            ProductAdminCreateRequest request = new ProductAdminCreateRequest(
                    "베이직 티셔츠",
                    29900,
                    "100% 면 소재",
                    "https://example.com/image.jpg",
                    categoryId
            );

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            ProductDetailResponse result = productAdminService.register(request);

            assertThat(result.name()).isEqualTo("베이직 티셔츠");
            assertThat(result.status().name()).isEqualTo("ACTIVE");
            assertThat(result.category().name()).isEqualTo(category.getName());

            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            ProductAdminCreateRequest request = new ProductAdminCreateRequest(
                    "베이직 티셔츠",
                    29900,
                    "설명",
                    "https://...",
                    999L
            );

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

            ProductAdminUpdateRequest request = new ProductAdminUpdateRequest("새 이름", 0, null, null, null);

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            ProductDetailResponse result = productAdminService.update(productId, request);

            assertThat(result.name()).isEqualTo("새 이름");
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            createProduct();

            assertThatThrownBy(() -> productAdminService.update(999L,
                    new ProductAdminUpdateRequest("이름", 0, null, null, null)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 단종 (discontinue)")
    class Discontinue {

        @Test
        @DisplayName("상품을 단종 처리하고, 장바구니에서 해당 상품을 삭제한다")
        void discontinueNormal() {
            // given
            Product product = createProduct();
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // when
            productAdminService.discontinue(productId);

            // then
            Product updatedProduct = productRepository.findById(productId)
                    .orElseThrow();

            assertThat(updatedProduct.getStatus().name()).isEqualTo("DISCONTINUED");
            verify(cartRepository).deleteAllItemsByProductId(productId);
        }
    }

    @Test
    @DisplayName("상품의 sku 추가")
    void addSku() {
        // given & when
        Product product = createProduct();
        setField(product, "id", productId);

        given(productRepository.findById(productId))
                .willReturn(Optional.of(product));

        SkuAdminAddRequest request = new SkuAdminAddRequest(
                List.of(
                        new SkuAdminAddRequest.SkuOptionRequest("색상", "검정"),
                        new SkuAdminAddRequest.SkuOptionRequest("사이즈", "L")
                ),
                100
        );

        SkuAdminResponse response = productAdminService.addSku(productId, request);

        // then
        assertThat(response.options().get(0).name()).isEqualTo("색상");
        assertThat(response.options().get(0).value()).isEqualTo("검정");
        assertThat(response.options().get(1).name()).isEqualTo("사이즈");
        assertThat(response.options().get(1).value()).isEqualTo("L");

        verify(productRepository).save(product);
    }
}