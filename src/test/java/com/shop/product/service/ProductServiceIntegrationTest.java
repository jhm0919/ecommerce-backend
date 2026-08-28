package com.shop.product.service;

import com.shop.category.domain.Category;
import com.shop.category.repository.CategoryRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.SkuOption;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.dto.ProductListResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    Long productId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.saveAndFlush(
                Category.create("남성 상의", "men-tops")
        );

        Product product = productRepository.saveAndFlush(
                Product.register("베이직 티셔츠", 29900, "100% 면 소재", "https://example.com/image.jpg", category)
        );

        product.addSku(
                List.of(
                        new SkuOption("색상", "검정"),
                        new SkuOption("사이즈", "L")
                ),
                100
        );
        product.addSku(
                List.of(
                        new SkuOption("색상", "빨강"),
                        new SkuOption("사이즈", "S")
                ),
                100
        );

        productRepository.saveAndFlush(product);

        productId = product.getId();
    }

    @Test
    @DisplayName("필터 없이 전체 조회")
    void allProducts() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<ProductListResponse> productList = productService.findProductList(null, null, pageable);

        assertThat(productList.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("카테고리 필터")
    void filterByCategoryId() {
        Product product = productRepository.findByIdWithSkus(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Pageable pageable = PageRequest.of(0, 20);

        Category category = product.getCategory();

        productRepository.saveAndFlush(
                Product.register("비싼 티셔츠", 49900, "울 소재", "https://example.com/image.jpg", category)
        );

        Page<ProductListResponse> productList = productService.findProductList(
                category.getId(), null, pageable);

        assertThat(productList.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("검색어로 조회")
    void searchByKeyword() {
        Product product = productRepository.findByIdWithSkus(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Pageable pageable = PageRequest.of(0, 20);

        Category category = product.getCategory();
        productRepository.saveAndFlush(
                Product.register("비싼 티셔츠", 49900, "울 소재", "https://example.com/image.jpg", category)
        );

        Page<ProductListResponse> result1 = productService.findProductList(
                null, "티셔츠", pageable);
        Page<ProductListResponse> result2 = productService.findProductList(
                null, "비싼", pageable);

        assertThat(result1.getContent()).hasSize(2);
        assertThat(result2.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("카테고리 + 검색어 조합")
    void filterByCategoryAndKeyword() {
        Product product = productRepository.findByIdWithSkus(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Pageable pageable = PageRequest.of(0, 20);

        Category category = product.getCategory();
        productRepository.saveAndFlush(
                Product.register("비싼 티셔츠", 49900, "울 소재", "https://example.com/image.jpg", category)
        );

        Page<ProductListResponse> result1 = productService.findProductList(category.getId(), "비싼", pageable);
        Page<ProductListResponse> result2 = productService.findProductList(category.getId(), "티셔츠", pageable);

        assertThat(result1.getContent()).hasSize(1);
        assertThat(result2.getContent()).hasSize(2);
    }


    @Test
    @DisplayName("ACTIVE 상품을 조회할 수 있다")
    void findActiveProduct() {
        ProductDetailResponse response = productService.findById(productId);

        for (ProductDetailResponse.SkuInfo sku : response.skus()) {
            for (ProductDetailResponse.OptionInfo skuOption : sku.options()) {
                System.out.println("option name = " + skuOption.name() + ", option value = " + skuOption.value());
            }
        }

        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.skus().get(0).options().get(0).name()).isEqualTo("색상");
        assertThat(response.skus().get(0).options().get(0).value()).isEqualTo("검정");
        assertThat(response.skus().get(0).options().get(1).name()).isEqualTo("사이즈");
        assertThat(response.skus().get(0).options().get(1).value()).isEqualTo("L");

        assertThat(response.skus().get(1).options().get(0).name()).isEqualTo("색상");
        assertThat(response.skus().get(1).options().get(0).value()).isEqualTo("빨강");
        assertThat(response.skus().get(1).options().get(1).name()).isEqualTo("사이즈");
        assertThat(response.skus().get(1).options().get(1).value()).isEqualTo("S");
    }

//    @Test
//    @DisplayName("SOLD_OUT 상품도 조회 가능 (사용자에게 노출됨)")
//    void findSoldOutProduct() {
//        Product product = productRepository.findByIdWithSkus(productId)
//                .orElseThrow(() -> new ProductNotFoundException(productId));
//
//        product.decreaseSkuStock(product.getSkus().get(0).getId(), 100);  // 모든 SKU 재고 0 → SOLD_OUT
//        product.decreaseSkuStock(product.getSkus().get(1).getId(), 100);  // 모든 SKU 재고 0 → SOLD_OUT
//
//        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
//    }


}

