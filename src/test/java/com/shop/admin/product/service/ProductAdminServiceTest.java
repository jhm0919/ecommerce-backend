package com.shop.admin.product.service;

import com.shop.admin.product.dto.ProductAdminCreateRequest;
import com.shop.admin.product.dto.ProductAdminUpdateRequest;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.repository.CategoryRepository;
import com.shop.category.service.CategoryService;
import com.shop.product.domain.Product;
import com.shop.product.domain.SkuOption;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import com.shop.product.service.ProductService;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ProductAdminServiceTest {
    @Autowired
    private ProductAdminService productAdminService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

        private Category createCategory() {
        Category category = Category.create("남성 상의", "men-tops");
        categoryRepository.saveAndFlush(category);
        return category;
    }

    private Product createProduct() {
        Product product = Product.register(
                "베이직 티셔츠",
                29900,
                "100% 면 소재",
                "https://example.com/image.jpg",
                createCategory()
        );
        productRepository.saveAndFlush(product);
        return product;
    }

    @Nested
    @DisplayName("상품 등록 (register)")
    class Register {

        @Test
        @DisplayName("정상적으로 상품을 등록할 수 있다")
        void registerNormal() {
            Category category = createCategory();
            System.out.println("category.getId() = " + category.getId());

            ProductAdminCreateRequest request = new ProductAdminCreateRequest(
                    "베이직 티셔츠",
                    29900,
                    "100% 면 소재",
                    "https://example.com/image.jpg",
                    category.getId()
            );

            ProductDetailResponse result = productAdminService.register(request);

            assertThat(result.name()).isEqualTo("베이직 티셔츠");
            assertThat(result.status().name()).isEqualTo("ACTIVE");
            assertThat(result.category().name()).isEqualTo(category.getName());
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

            ProductDetailResponse result = productAdminService.update(product.getId(), request);

            assertThat(result.name()).isEqualTo("새 이름");
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

            // when
            productAdminService.discontinue(product.getId());

            // then
            Product updatedProduct = productRepository.findById(product.getId())
                    .orElseThrow();
            assertThat(updatedProduct.getStatus().name()).isEqualTo("DISCONTINUED");
        }
    }

    @Test
    @DisplayName("상품의 sku 추가")
    void addSku() {
        // given & when
        Product product = createProduct();

        product.addSku(
                List.of(
                        new SkuOption("색상", "검정"),
                        new SkuOption("사이즈", "L")
                ),
                100
        );

        Product savedSkuProduct = productRepository.save(product);

        // then
        assertThat(savedSkuProduct.getSkus().get(0).getOptions().get(0).getOptionName()).isEqualTo("색상");
        assertThat(savedSkuProduct.getSkus().get(0).getOptions().get(0).getOptionValue()).isEqualTo("검정");
        assertThat(savedSkuProduct.getSkus().get(0).getOptions().get(1).getOptionName()).isEqualTo("사이즈");
        assertThat(savedSkuProduct.getSkus().get(0).getOptions().get(1).getOptionValue()).isEqualTo("L");
    }
}