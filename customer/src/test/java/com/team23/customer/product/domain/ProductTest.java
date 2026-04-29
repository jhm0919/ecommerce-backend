package com.team23.customer.product.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private Category category;
    private Money validPrice;

    @BeforeEach
    void setUp() {
        category = Category.create("남성 상의", "men-tops");
        validPrice = Money.krw(29900);  // ★ 사용자 Money 활용
    }

    private Product createProduct(int stock) {
        return Product.register(
                "베이직 티셔츠",
                validPrice,
                stock,
                "100% 면 소재",
                "https://example.com/image.jpg",
                category
        );
    }

    @Nested
    @DisplayName("등록 (register)")
    class Register {

        @Test
        @DisplayName("재고가 있으면 ACTIVE 상태로 등록된다")
        void registerActiveWhenInStock() {
            Product product = createProduct(10);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.getStock()).isEqualTo(10);
            assertThat(product.getName()).isEqualTo("베이직 티셔츠");
            assertThat(product.getPrice()).isEqualTo(validPrice);
        }

        @Test
        @DisplayName("재고가 0이면 SOLD_OUT 상태로 등록된다")
        void registerSoldOutWhenNoStock() {
            Product product = createProduct(0);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
            assertThat(product.getStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("name이 null/blank면 예외")
        void rejectInvalidName() {
            assertThatThrownBy(() -> Product.register(
                    null, validPrice, 10, "desc", "img", category))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> Product.register(
                    "  ", validPrice, 10, "desc", "img", category))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("price가 null이면 예외")
        void rejectNullPrice() {
            assertThatThrownBy(() -> Product.register(
                    "이름", null, 10, "desc", "img", category))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("stock이 음수면 예외")
        void rejectNegativeStock() {
            assertThatThrownBy(() -> Product.register(
                    "이름", validPrice, -1, "desc", "img", category))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("category가 null이면 예외")
        void rejectNullCategory() {
            assertThatThrownBy(() -> Product.register(
                    "이름", validPrice, 10, "desc", "img", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("description은 null도 가능 (선택 사항)")
        void allowNullDescription() {
            Product product = Product.register(
                    "이름", validPrice, 10, null, "img", category);

            assertThat(product.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("재고 증가 (increaseStock)")
    class IncreaseStock {

        @Test
        @DisplayName("재고를 증가시킬 수 있다")
        void increaseStockNormal() {
            Product product = createProduct(10);

            product.increaseStock(5);

            assertThat(product.getStock()).isEqualTo(15);
        }

        @Test
        @DisplayName("SOLD_OUT 상품에 재고 추가하면 ACTIVE로 전환")
        void soldOutToActive() {
            Product product = createProduct(0);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            product.increaseStock(5);

            assertThat(product.getStock()).isEqualTo(5);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 재고 추가 불가")
        void cannotIncreaseDiscontinued() {
            Product product = createProduct(10);
            product.discontinue();

            assertThatThrownBy(() -> product.increaseStock(5))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("0 또는 음수면 예외")
        void rejectInvalidQuantity() {
            Product product = createProduct(10);

            assertThatThrownBy(() -> product.increaseStock(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> product.increaseStock(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("재고 감소 (decreaseStock)")
    class DecreaseStock {

        @Test
        @DisplayName("재고를 감소시킬 수 있다")
        void decreaseStockNormal() {
            Product product = createProduct(10);

            product.decreaseStock(3);

            assertThat(product.getStock()).isEqualTo(7);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("재고가 0이 되면 SOLD_OUT으로 자동 전환")
        void allStockSoldOut() {
            Product product = createProduct(5);

            product.decreaseStock(5);

            assertThat(product.getStock()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("재고보다 많이 감소시키려 하면 예외")
        void rejectInsufficientStock() {
            Product product = createProduct(5);

            assertThatThrownBy(() -> product.decreaseStock(10))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("SOLD_OUT 상품의 재고 감소는 불가")
        void cannotDecreaseSoldOut() {
            Product product = createProduct(0);

            assertThatThrownBy(() -> product.decreaseStock(1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("DISCONTINUED 상품의 재고 감소는 불가")
        void cannotDecreaseDiscontinued() {
            Product product = createProduct(10);
            product.discontinue();

            assertThatThrownBy(() -> product.decreaseStock(1))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("단종 (discontinue)")
    class Discontinue {

        @Test
        @DisplayName("ACTIVE 상품을 단종 처리할 수 있다")
        void discontinueActive() {
            Product product = createProduct(10);

            product.discontinue();

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("SOLD_OUT 상품도 단종 처리 가능")
        void discontinueSoldOut() {
            Product product = createProduct(0);

            product.discontinue();

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("이미 DISCONTINUED인 상품은 다시 단종 불가")
        void cannotDiscontinueAlready() {
            Product product = createProduct(10);
            product.discontinue();

            assertThatThrownBy(product::discontinue)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("정보 수정")
    class UpdateInfo {

        @Test
        @DisplayName("name만 수정할 수 있다")
        void updateNameOnly() {
            Product product = createProduct(10);

            product.updateInfo("새 이름", null, null);

            assertThat(product.getName()).isEqualTo("새 이름");
            assertThat(product.getDescription()).isEqualTo("100% 면 소재");
            assertThat(product.getMainImageUrl()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("description만 수정할 수 있다")
        void updateDescriptionOnly() {
            Product product = createProduct(10);

            product.updateInfo(null, "새 설명", null);

            assertThat(product.getName()).isEqualTo("베이직 티셔츠");
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }
    }

    @Nested
    @DisplayName("가격 변경")
    class ChangePrice {

        @Test
        @DisplayName("가격을 변경할 수 있다")
        void changePriceNormal() {
            Product product = createProduct(10);
            Money newPrice = Money.krw(39900);  // ★ 사용자 Money

            product.changePrice(newPrice);

            assertThat(product.getPrice()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 가격 변경 불가")
        void cannotChangePriceOfDiscontinued() {
            Product product = createProduct(10);
            product.discontinue();
            Money newPrice = Money.krw(39900);  // ★ 사용자 Money

            assertThatThrownBy(() -> product.changePrice(newPrice))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}