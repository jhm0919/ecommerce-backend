package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CartItemTest {

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");
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
    @DisplayName("Product와 수량으로 CartItem 생성")
    void createFromProduct() {
        CartItem item = createCartItem(2);

        assertThat(item.getProductId()).isEqualTo(product.getId());
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
        assertThatThrownBy(() -> CartItem.of(null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * package-private 메서드라 같은 패키지에서만 호출 가능.
     */
    private CartItem createCartItem(int quantity) {
        return CartItem.of(product, quantity);
    }
}