package com.team23.customer.order.domain;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderItemTest {

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = Category.create("남성 상의", "men-tops");
        product = Product.register(
                "베이직 티셔츠",
                Money.krw(29900),
                100,
                "100% 면",
                "https://example.com/image.jpg",
                category
        );
    }

    @Test
    @DisplayName("Product와 수량으로 OrderItem 생성")
    void createFromProduct() {
        OrderItem item = OrderItem.of(product, 2);

        assertThat(item.getProductName()).isEqualTo("베이직 티셔츠");
        assertThat(item.getPriceAtOrder()).isEqualTo(Money.krw(29900));
        assertThat(item.getProductImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("스냅샷이라 Product 변경에 영향 X")
    void snapshotIsolatedFromProductChanges() {
        OrderItem item = OrderItem.of(product, 1);
        Money originalPrice = item.getPriceAtOrder();

        // Product 가격 변경
        product.changePrice(Money.krw(99900));

        // OrderItem 가격은 그대로 (스냅샷)
        assertThat(item.getPriceAtOrder()).isEqualTo(originalPrice);
        assertThat(item.getPriceAtOrder()).isNotEqualTo(Money.krw(99900));
    }

    @Test
    @DisplayName("소계 계산")
    void calculateSubtotal() {
        OrderItem item = OrderItem.of(product, 3);

        Money subtotal = item.calculateSubtotal();

        assertThat(subtotal).isEqualTo(Money.krw(89700));  // 29900 × 3
    }

    @Test
    @DisplayName("수량이 0이면 예외")
    void rejectZeroQuantity() {
        assertThatThrownBy(() -> OrderItem.of(product, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수량이 음수면 예외")
    void rejectNegativeQuantity() {
        assertThatThrownBy(() -> OrderItem.of(product, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Product가 null이면 예외")
    void rejectNullProduct() {
        assertThatThrownBy(() -> OrderItem.of(null, 1))
                .isInstanceOf(NullPointerException.class);
    }
}