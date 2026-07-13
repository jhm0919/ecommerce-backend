package com.shop.cart.domain;

import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CartItemTest {

    private Product product;
    private Sku sku;

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");
        product = Product.register(
                "베이직 티셔츠",
                BigDecimal.valueOf(29900),
                "100% 면",
                "https://example.com/image.jpg",
                category
        );
        setId(product, 1L);
        sku = product.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setId(sku, 100L);
    }

    @Test
    @DisplayName("Product, SKU, 수량으로 CartItem 생성")
    void createFromProduct() {
        CartItem item = createCartItem(2);

        assertThat(item.getProductId()).isEqualTo(product.getId());
        assertThat(item.getSkuId()).isEqualTo(sku.getId());  // ★ skuId 검증
        assertThat(item.getProductName()).isEqualTo("베이직 티셔츠");
        assertThat(item.getProductImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("수량이 0이면 예외")
    void rejectZeroQuantity() {
        assertThatThrownBy(() -> createCartItem(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수량이 음수면 예외")
    void rejectNegativeQuantity() {
        assertThatThrownBy(() -> createCartItem(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수량이 100을 초과하면 예외")
    void rejectExceedingMax() {
        assertThatThrownBy(() -> createCartItem(101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Product가 null이면 예외")
    void rejectNullProduct() {
        assertThatThrownBy(() -> CartItem.of(null, sku, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("SKU가 null이면 예외")  // ★ 새 테스트
    void rejectNullSku() {
        assertThatThrownBy(() -> CartItem.of(product, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("isSameSku — 같은 skuId면 true")  // ★ 새 테스트
    void isSameSkuTrue() {
        CartItem item = createCartItem(1);

        assertThat(item.isSameSku(100L)).isTrue();
        assertThat(item.isSameSku(999L)).isFalse();
    }

    // ─── 테스트 헬퍼 ───

    /**
     * package-private 메서드라 같은 패키지에서만 호출 가능.
     */
    private CartItem createCartItem(int quantity) {
        return CartItem.of(product, sku, quantity);
    }

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