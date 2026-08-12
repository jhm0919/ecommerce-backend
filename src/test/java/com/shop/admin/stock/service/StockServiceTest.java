package com.shop.admin.stock.service;

import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks private StockService stockService;

    @Mock private ProductRepository productRepository;

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

    @Nested
    @DisplayName("SKU 재고 증가 (increaseStock)")
    class IncreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 증가시킨다")
        void increaseSkuStockNormal() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findByIdWithSkus(1L)).willReturn(Optional.of(product));

            stockService.increaseStock(1L, 100L, 5);

            assertThat(sku.getQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findByIdWithSkus(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.increaseStock(999L, 100L, 5))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }



    @Test
    @DisplayName("SKU 재고를 감소시킨다")
    void decreaseStockNormal() {
        Product product = createProduct();
        setId(product, 1L);
        Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
        setId(sku, 100L);

        given(productRepository.findByIdWithSkus(1L)).willReturn(Optional.of(product));

        stockService.decreaseStock(1L, 100L, 3);

        assertThat(sku.getQuantity()).isEqualTo(7);
    }

}