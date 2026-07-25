package com.shop.order.domain;

import com.shop.category.domain.Category;
import com.shop.product.domain.Money;
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

class OrderItemTest {

    private Product product;
    private Sku sku;

    @BeforeEach
    void setUp() {
        Category category = Category.create("남성 상의", "men-tops");
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
    @DisplayName("Product와 SKU로 OrderItem 생성")
    void createFromProduct() {
        OrderItem item = OrderItem.of(product, sku, 2);

        assertThat(item.getProductName()).isEqualTo("베이직 티셔츠");
        assertThat(item.getPriceAtOrder()).isEqualTo(new Money(BigDecimal.valueOf(29900)));
        assertThat(item.getProductImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getSkuCode()).isEqualTo("SKU-1-001");
        assertThat(item.getSkuOptions()).hasSize(1);
    }

    @Test
    @DisplayName("SKU 옵션 정보가 스냅샷으로 저장된다")
    void skuOptionsAreSnapshot() {
        OrderItem item = OrderItem.of(product, sku, 1);

        assertThat(item.getSkuOptions())
                .extracting(opt -> opt.getOptionName() + "=" + opt.getOptionValue())
                .containsExactly("색상=검정");
    }

    @Test
    @DisplayName("스냅샷이라 Product 변경에 영향 X")
    void snapshotIsolatedFromProductChanges() {
        OrderItem item = OrderItem.of(product, sku, 1);
        Money originalPrice = item.getPriceAtOrder();

        // Product 가격 변경
        product.update(null, null, null, BigDecimal.valueOf(99900), null);

        // OrderItem 가격은 그대로 (스냅샷)
        assertThat(item.getPriceAtOrder()).isEqualTo(originalPrice);
        assertThat(item.getPriceAtOrder()).isNotEqualTo(BigDecimal.valueOf(99900));
    }

    @Test
    @DisplayName("소계 계산")
    void calculateSubtotal() {
        OrderItem item = OrderItem.of(product, sku, 3);

        Money subtotal = item.calculateSubtotal();

        assertThat(subtotal).isEqualTo(new Money(BigDecimal.valueOf(89700)));  // 29900 × 3
    }

    @Test
    @DisplayName("수량이 0이면 예외")
    void rejectZeroQuantity() {
        assertThatThrownBy(() -> OrderItem.of(product, sku, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수량이 음수면 예외")
    void rejectNegativeQuantity() {
        assertThatThrownBy(() -> OrderItem.of(product, sku, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Product가 null이면 예외")
    void rejectNullProduct() {
        assertThatThrownBy(() -> OrderItem.of(null, sku, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("SKU가 null이면 예외")
    void rejectNullSku() {
        assertThatThrownBy(() -> OrderItem.of(product, null, 1))
                .isInstanceOf(NullPointerException.class);
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