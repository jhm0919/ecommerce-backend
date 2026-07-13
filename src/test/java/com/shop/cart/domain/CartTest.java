package com.shop.cart.domain;

import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CartTest {

    private Product product1;
    private Product product2;
    private Sku sku1;   // product1의 SKU (검정/S)
    private Sku sku1B;  // product1의 다른 SKU (흰색/M) — 같은 상품 다른 옵션 테스트용
    private Sku sku2;   // product2의 SKU

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");

        product1 = Product.register(
                "티셔츠", BigDecimal.valueOf(29900), "설명", "img1", category
        );
        setId(product1, 1L);
        sku1 = product1.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setId(sku1, 100L);
        sku1B = product1.addSku(List.of(new SkuOption("색상", "흰색")), 30);
        setId(sku1B, 101L);

        product2 = Product.register(
                "바지", BigDecimal.valueOf(49900), "설명", "img2", category
        );
        setId(product2, 2L);
        sku2 = product2.addSku(List.of(new SkuOption("색상", "회색")), 40);
        setId(sku2, 200L);
    }

    @Nested
    @DisplayName("생성 (createFor)")
    class Create {

        @Test
        @DisplayName("회원 장바구니를 생성할 수 있다")
        void createForMember() {
            Cart cart = Cart.createFor(1L);

            assertThat(cart.getMemberId()).isEqualTo(1L);
            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.getItemCount()).isZero();
        }

        @Test
        @DisplayName("memberId가 null이면 예외")
        void rejectNullMemberId() {
            assertThatThrownBy(() -> Cart.createFor(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("상품 추가 (addItem)")
    class AddItem {

        @Test
        @DisplayName("새 상품을 추가할 수 있다")
        void addNewItem() {
            Cart cart = Cart.createFor(1L);

            cart.addItem(product1, sku1, 2);

            assertThat(cart.getItemCount()).isEqualTo(1);
            assertThat(cart.getTotalQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("같은 SKU를 다시 추가하면 수량 합산")
        void addSameSkuIncreasesQuantity() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);

            cart.addItem(product1, sku1, 3);

            assertThat(cart.getItemCount()).isEqualTo(1);  // 종류는 그대로
            assertThat(cart.getTotalQuantity()).isEqualTo(5);  // 2 + 3
        }

        @Test
        @DisplayName("같은 Product라도 SKU가 다르면 별도 항목")  // ★ 핵심 새 테스트
        void addSameProductDifferentSkuCreatesNewItem() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);   // 티셔츠 검정

            cart.addItem(product1, sku1B, 1);  // 티셔츠 흰색

            assertThat(cart.getItemCount()).isEqualTo(2);  // 별도 항목
            assertThat(cart.getTotalQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("다른 상품을 추가하면 별도 항목")
        void addDifferentProduct() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);

            cart.addItem(product2, sku2, 1);

            assertThat(cart.getItemCount()).isEqualTo(2);
            assertThat(cart.getTotalQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("합산 결과가 100을 초과하면 예외")
        void rejectExceedingMaxAfterAdd() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 99);

            assertThatThrownBy(() -> cart.addItem(product1, sku1, 2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("수량 변경 (changeItemQuantity)")
    class ChangeItemQuantity {

        @Test
        @DisplayName("항목의 수량을 변경할 수 있다")
        void changeQuantity() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);
            CartItem item = cart.getItems().get(0);
            setItemId(item, 10L);

            cart.changeItemQuantity(10L, 5);

            assertThat(cart.getTotalQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 항목 ID는 예외")
        void rejectUnknownItemId() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);

            assertThatThrownBy(() -> cart.changeItemQuantity(999L, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("수량 0은 예외 (DELETE 사용)")
        void rejectZeroQuantity() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);
            CartItem item = cart.getItems().get(0);
            setItemId(item, 10L);

            assertThatThrownBy(() -> cart.changeItemQuantity(10L, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("항목 제거 (removeItem)")
    class RemoveItem {

        @Test
        @DisplayName("항목을 제거할 수 있다")
        void removeExisting() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);
            cart.addItem(product2, sku2, 1);

            CartItem firstItem = cart.getItems().get(0);
            setItemId(firstItem, 10L);
            CartItem secondItem = cart.getItems().get(1);
            setItemId(secondItem, 20L);

            cart.removeItem(10L);

            assertThat(cart.getItemCount()).isEqualTo(1);
            assertThat(cart.getItems().get(0).getProductId()).isEqualTo(product2.getId());
        }

        @Test
        @DisplayName("존재하지 않는 항목 ID는 예외")
        void rejectUnknownItemId() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);

            assertThatThrownBy(() -> cart.removeItem(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("전체 비우기 (clear)")
    class Clear {

        @Test
        @DisplayName("모든 항목을 제거")
        void clearAll() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 2);
            cart.addItem(product2, sku2, 1);

            cart.clear();

            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.getItemCount()).isZero();
        }

        @Test
        @DisplayName("빈 카트를 비워도 에러 없음")
        void clearEmptyCart() {
            Cart cart = Cart.createFor(1L);

            assertThatCode(cart::clear).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("항목 보호")
    class ItemProtection {

        @Test
        @DisplayName("getItems는 불변 복사본 반환")
        void itemsAreImmutable() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, sku1, 1);

            List<CartItem> items = cart.getItems();

            assertThatThrownBy(() -> items.add(CartItem.of(product2, sku2, 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ─────────────────────────────────────
    // 테스트 헬퍼
    // ─────────────────────────────────────

    private static void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setItemId(CartItem item, Long id) {
        try {
            Field idField = CartItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}