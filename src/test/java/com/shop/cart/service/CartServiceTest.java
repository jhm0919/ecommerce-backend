package com.shop.cart.service;

import com.shop.cart.domain.Cart;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductNotPurchasableException;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;

    private CartService cartService; // 필드만 선언합니다.

    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long SKU_ID = 200L;

    private Product product;
    private Sku sku;

    @BeforeEach
    void setUp() {
        // 테스트 시작 전에 서비스 객체를 수동으로 생성하고 모든 Mock을 주입
        cartService = new CartService(cartRepository, productRepository);

        Category category = Category.create("의류", "clothing");
        product = Product.register(
                "티셔츠", 29900, "설명", "img", category
        );
        setId(product, PRODUCT_ID);
        sku = product.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setId(sku, SKU_ID);
    }

    @Nested
    @DisplayName("내 장바구니 조회")
    class GetMyCart {

        @Test
        @DisplayName("기존 카트가 있으면 그대로 반환")
        void returnsExistingCart() {
            Cart existingCart = Cart.createFor(MEMBER_ID);
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(existingCart));

            CartService.CartView view = cartService.getMyCart(MEMBER_ID);

            assertThat(view.cart()).isEqualTo(existingCart);
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("카트가 없으면 새로 생성")
        void createsNewCart() {
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            CartService.CartView view = cartService.getMyCart(MEMBER_ID);

            assertThat(view.cart().getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(view.cart().isEmpty()).isTrue();
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("상품 추가")
    class AddItem {

        @Test
        @DisplayName("정상적으로 상품 추가")
        void addNormal() {
            Cart cart = Cart.createFor(MEMBER_ID);

            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(cart));
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            CartService.CartView view = cartService.addItem(MEMBER_ID, PRODUCT_ID, SKU_ID, 2);

            assertThat(view.cart().getItemCount()).isEqualTo(1);
            assertThat(view.cart().getTotalQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("같은 SKU 다시 추가 시 합산")
        void mergeSameSku() {
            Cart cart = Cart.createFor(MEMBER_ID);
            cart.addItem(product, sku, 2);  // 미리 2개 담음

            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(cart));
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            CartService.CartView view = cartService.addItem(MEMBER_ID, PRODUCT_ID, SKU_ID, 3);

            assertThat(view.cart().getItemCount()).isEqualTo(1);
            assertThat(view.cart().getTotalQuantity()).isEqualTo(5);  // 2 + 3
        }

        @Test
        @DisplayName("존재하지 않는 상품은 ProductNotFoundException")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(MEMBER_ID, 999L, SKU_ID, 1))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 SKU ID는 예외")  // ★ 새 테스트
        void rejectUnknownSku() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItem(MEMBER_ID, PRODUCT_ID, 999L, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("단종 상품은 ProductNotPurchasableException")
        void rejectDiscontinued() {
            product.discontinue();

            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItem(MEMBER_ID, PRODUCT_ID, SKU_ID, 1))
                    .isInstanceOf(ProductNotPurchasableException.class);
        }

        @Test
        @DisplayName("카트가 없으면 자동 생성 후 추가")
        void createsCartIfMissing() {
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            CartService.CartView view = cartService.addItem(MEMBER_ID, PRODUCT_ID, SKU_ID, 1);

            assertThat(view.cart().getItemCount()).isEqualTo(1);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("수량 변경")
    class ChangeQuantity {

        @Test
        @DisplayName("정상적으로 수량 변경")
        void changeNormal() {
            Cart cart = Cart.createFor(MEMBER_ID);
            cart.addItem(product, sku, 2);
            setItemId(cart.getItems().get(0), 10L);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(cart));
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            CartService.CartView view = cartService.changeItemQuantity(MEMBER_ID, 10L, 5);

            assertThat(view.cart().getTotalQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 항목은 CartItemNotFoundException")
        void rejectUnknownItem() {
            Cart cart = Cart.createFor(MEMBER_ID);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(cart));

            assertThatThrownBy(() -> cartService.changeItemQuantity(MEMBER_ID, 999L, 5))
                    .isInstanceOf(CartItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("항목 삭제")
    class RemoveItem {

        @Test
        @DisplayName("정상적으로 항목 삭제")
        void removeNormal() {
            Cart cart = Cart.createFor(MEMBER_ID);
            cart.addItem(product, sku, 2);
            setItemId(cart.getItems().get(0), 10L);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(cart));

            CartService.CartView view = cartService.removeItem(MEMBER_ID, 10L);

            assertThat(view.cart().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("전체 비우기")
    class ClearCart {

        @Test
        @DisplayName("카트가 있으면 비움")
        void clearExisting() {
            Cart cart = Cart.createFor(MEMBER_ID);
            cart.addItem(product, sku, 2);

            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.of(cart));

            cartService.clearMyCart(MEMBER_ID);

            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("카트가 없어도 에러 없음 (멱등성)")
        void clearMissingIsSilent() {
            given(cartRepository.findByMemberIdWithItems(MEMBER_ID))
                    .willReturn(Optional.empty());

            assertThatCode(() -> cartService.clearMyCart(MEMBER_ID))
                    .doesNotThrowAnyException();
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

    private static void setItemId(Object item, Long id) {
        try {
            Field idField = item.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}