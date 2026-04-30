package com.team23.customer.cart.domain;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CartTest {

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");
        product1 = Product.register(
                "티셔츠", Money.krw(29900), 100, "설명", "img1", category
        );
        // 테스트를 위해 Product의 ID를 강제 설정 (DB 저장 안 한 상태)
        setId(product1, 1L);

        product2 = Product.register(
                "바지", Money.krw(49900), 50, "설명", "img2", category
        );
        setId(product2, 2L);
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

            cart.addItem(product1, 2);

            assertThat(cart.getItemCount()).isEqualTo(1);
            assertThat(cart.getTotalQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("같은 상품을 다시 추가하면 수량 합산")
        void addExistingItemIncreasesQuantity() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, 2);

            cart.addItem(product1, 3);

            assertThat(cart.getItemCount()).isEqualTo(1);  // 종류는 그대로
            assertThat(cart.getTotalQuantity()).isEqualTo(5);  // 2 + 3
        }

        @Test
        @DisplayName("다른 상품을 추가하면 별도 항목")
        void addDifferentProduct() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, 2);

            cart.addItem(product2, 1);

            assertThat(cart.getItemCount()).isEqualTo(2);
            assertThat(cart.getTotalQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("합산 결과가 100을 초과하면 예외")
        void rejectExceedingMaxAfterAdd() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, 99);

            assertThatThrownBy(() -> cart.addItem(product1, 2))
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
            cart.addItem(product1, 2);
            CartItem item = cart.getItems().get(0);
            setItemId(item, 10L);

            cart.changeItemQuantity(10L, 5);

            assertThat(cart.getTotalQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 항목 ID는 예외")
        void rejectUnknownItemId() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, 2);

            assertThatThrownBy(() -> cart.changeItemQuantity(999L, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("수량 0은 예외 (DELETE 사용)")
        void rejectZeroQuantity() {
            Cart cart = Cart.createFor(1L);
            cart.addItem(product1, 2);
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
            cart.addItem(product1, 2);
            cart.addItem(product2, 1);

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
            cart.addItem(product1, 2);

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
            cart.addItem(product1, 2);
            cart.addItem(product2, 1);

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
            cart.addItem(product1, 1);

            List<CartItem> items = cart.getItems();

            assertThatThrownBy(() -> items.add(CartItem.of(product2, 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ─────────────────────────────────────
    // 테스트 헬퍼 (리플렉션으로 ID 설정)
    // ─────────────────────────────────────

    /**
     * 테스트 목적으로 Product의 ID를 설정.
     * 실제 환경에서는 DB가 자동 부여하지만, 단위 테스트에서는 수동 설정 필요.
     */
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