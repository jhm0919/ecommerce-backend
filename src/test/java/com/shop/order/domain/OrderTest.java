package com.shop.order.domain;

import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private Product product1;
    private Product product2;
    private Sku sku1;
    private Sku sku2;

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");

        product1 = Product.register(
                "티셔츠", 29900, "설명", "img1", category
        );
        setId(product1, 1L);
        sku1 = product1.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setId(sku1, 100L);

        product2 = Product.register(
                "바지", 49900, "설명", "img2", category
        );
        setId(product2, 2L);
        sku2 = product2.addSku(List.of(new SkuOption("색상", "회색")), 30);
        setId(sku2, 200L);
    }

    private List<OrderItem> singleItem() {
        return List.of(OrderItem.of(product1, sku1, 2));
    }

    private List<OrderItem> multipleItems() {
        return List.of(
                OrderItem.of(product1, sku1, 2),
                OrderItem.of(product2, sku2, 1)
        );
    }

    @Nested
    @DisplayName("회원 주문 생성")
    class CreateForMember {

        @Test
        @DisplayName("회원 주문을 생성할 수 있다")
        void createMemberOrder() {
            Order order = Order.createForMember(1L, singleItem());

            assertThat(order.getMemberId()).isEqualTo(1L);
            assertThat(order.isMemberOrder()).isTrue();
            assertThat(order.isGuestOrder()).isFalse();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getOrderNumber()).startsWith("ORD-");
        }

        @Test
        @DisplayName("총 금액이 계산된다")
        void calculatesTotalAmount() {
            Order order = Order.createForMember(1L, multipleItems());

            // 29900 × 2 + 49900 × 1 = 109,700

            assertThat(order.getTotalPrice()).isEqualTo(109700);
        }

        @Test
        @DisplayName("memberId가 null이면 예외")
        void rejectNullMemberId() {
            assertThatThrownBy(() -> Order.createForMember(null, singleItem()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("아이템이 비어있으면 예외")
        void rejectEmptyItems() {
            assertThatThrownBy(() -> Order.createForMember(1L, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소 (cancel)")
    class Cancel {

        @Test
        @DisplayName("PENDING 주문을 취소할 수 있다")
        void cancelPending() {
            Order order = Order.createForMember(1L, singleItem());

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 취소된 주문은 다시 취소 불가")
        void cannotCancelAlreadyCancelled() {
            Order order = Order.createForMember(1L, singleItem());
            order.cancel();

            assertThatThrownBy(order::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("주문 항목 보호")
    class ItemProtection {

        @Test
        @DisplayName("getItems는 불변 복사본을 반환한다")
        void itemsAreImmutable() {
            Order order = Order.createForMember(1L, multipleItems());

            List<OrderItem> items = order.getItems();

            assertThatThrownBy(() -> items.add(OrderItem.of(product1, sku1, 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
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