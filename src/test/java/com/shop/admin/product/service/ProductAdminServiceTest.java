package com.shop.admin.product.service;

import com.shop.admin.product.dto.ProductAdminCreateRequest;
import com.shop.admin.product.dto.ProductAdminUpdateRequest;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.repository.CategoryRepository;
import com.shop.product.domain.Product;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CartRepository cartRepository; // 1. CartRepository Mock 객체 추가
    private ProductAdminService productAdminService; // 필드만 선언

    @BeforeEach
        // 4. @BeforeEach 셋업 메서드 추가 (또는 기존 메서드에 추가)
    void setUp() {
        // 5. 서비스 객체를 수동으로 생성하고 모든 Mock을 주입합니다.
        productAdminService = new ProductAdminService(
                productRepository,
                categoryRepository,
                cartRepository,
                eventPublisher
        );
    }

    private Category createCategory() {
        return Category.create("남성 상의", "men-tops");
    }

    private Product createProduct() {
        return Product.register(
                "베이직 티셔츠",
                BigDecimal.valueOf(29900),
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
            ProductAdminCreateRequest request = new ProductAdminCreateRequest(
                    "베이직 티셔츠",
                    new BigDecimal("29900"),
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
            ProductAdminCreateRequest request = new ProductAdminCreateRequest(
                    "베이직 티셔츠",
                    new BigDecimal("29900"),
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
            Long categoryId = product.getCategory().getId();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            ProductDetailResponse result = productAdminService.update(1L,
                    new ProductAdminUpdateRequest("새 이름", null, null, null, null));

            assertThat(result.name()).isEqualTo("새 이름");
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productAdminService.update(999L,
                    new ProductAdminUpdateRequest("이름", null, null, null, null)))
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
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            // when
            productAdminService.discontinue(1L);

            // then
            assertThat(product.getStatus().name()).isEqualTo("DISCONTINUED");
            // cartRepository의 deleteAllItemsByProductId 메서드가 1L을 인자로 하여 한 번 호출되었는지 검증
            verify(cartRepository).deleteAllItemsByProductId(1L);
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