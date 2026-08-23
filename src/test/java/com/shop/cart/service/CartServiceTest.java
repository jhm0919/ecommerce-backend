package com.shop.cart.service;

import com.shop.cart.domain.Cart;
import com.shop.cart.domain.CartItem;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductNotPurchasableException;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.repository.CategoryRepository;
import com.shop.member.repository.MemberRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    private final Long productId = 1L;
    private final Long memberId = 1L;
    private final Long skuId = 2L;

    private Product product;
    private Sku sku;

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");

        product = Product.register(
                "티셔츠", 29900, "설명", "img", category
        );
        setField(product, "id", productId);

        sku = product.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setField(sku, "id", skuId);
    }

    @Nested
    @DisplayName("내 장바구니 조회")
    class GetMyCart {

        @Test
        @DisplayName("기존 카트가 있으면 그대로 반환")
        void returnsExistingCart() {
            Cart existingCart = Cart.createFor(memberId);
            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(existingCart));

            CartResponse response = cartService.getMyCart(memberId);

            assertThat(response.cartId()).isEqualTo(existingCart.getId());
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("카트가 없으면 새로 생성")
        void createsNewCart() {
            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            CartResponse response = cartService.getMyCart(memberId);

            assertThat(response.itemCount()).isZero();
            assertThat(response.totalQuantity()).isZero();
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("상품 추가")
    class AddItem {

        @Test
        @DisplayName("정상적으로 상품 추가")
        void addNormal() {
            Cart cart = Cart.createFor(memberId);

            given(productRepository.findByIdWithSkus(productId)).willReturn(Optional.of(product));
            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(cart));

            CartResponse response = cartService.addItem(memberId, productId, skuId, 2);

            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.totalQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("같은 SKU 다시 추가 시 합산")
        void mergeSameSku() {
            Cart cart = Cart.createFor(memberId);

            given(productRepository.findByIdWithSkus(productId)).willReturn(Optional.of(product));
            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(cart));

            cartService.addItem(memberId, productId, skuId, 2); // 미리 2개 담음

            CartResponse response = cartService.addItem(memberId, productId, skuId, 3);

            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.totalQuantity()).isEqualTo(5);  // 2 + 3
        }

        @Test
        @DisplayName("존재하지 않는 상품은 ProductNotFoundException")
        void rejectUnknownProduct() {
            given(productRepository.findByIdWithSkus(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(memberId, 999L, skuId, 1))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("단종 상품은 ProductNotPurchasableException")
        void rejectDiscontinued() {
            product.discontinue();

            given(productRepository.findByIdWithSkus(productId)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItem(memberId, productId, skuId, 1))
                    .isInstanceOf(ProductNotPurchasableException.class);
        }

        @Test
        @DisplayName("카트가 없으면 자동 생성 후 추가")
        void createsCartIfMissing() {
            given(productRepository.findByIdWithSkus(productId)).willReturn(Optional.of(product));
            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            CartResponse response = cartService.addItem(memberId, productId, skuId, 1);

            assertThat(response.itemCount()).isEqualTo(1);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("수량 변경")
    class ChangeQuantity {

        @Test
        @DisplayName("정상적으로 수량 변경")
        void changeNormal() {
            Cart cart = Cart.createFor(memberId);

            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(cart));
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            cart.addItem(product, sku, 2);

            CartItem item = cart.getItems().get(0);
            Long itemId = 300L;
            setField(item, "id", itemId);

            CartResponse response = cartService.changeItemQuantity(memberId, itemId, 5);

            assertThat(response.totalQuantity()).isEqualTo(5);
            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.items().get(0).quantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 항목은 CartItemNotFoundException")
        void rejectUnknownItem() {
            Cart cart = Cart.createFor(memberId);

            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(cart));

            assertThatThrownBy(() -> cartService.changeItemQuantity(memberId, 999L, 5))
                    .isInstanceOf(CartItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("항목 삭제")
    class RemoveItem {

        @Test
        @DisplayName("정상적으로 항목 삭제")
        void removeNormal() {
            Cart cart = Cart.createFor(memberId);
            cart.addItem(product, sku, 2);

            Long itemId = 10L;
            setField(cart.getItems().get(0), "id", itemId);

            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(cart));

            CartResponse response = cartService.removeItem(memberId, itemId);

            assertThat(response.itemCount()).isEqualTo(0);
            assertThat(response.totalQuantity()).isZero();
            assertThat(cart.isEmpty()).isTrue();
            verify(cartRepository).findByMemberIdWithItems(memberId);
        }
    }

    @Nested
    @DisplayName("전체 비우기")
    class ClearCart {

        @Test
        @DisplayName("카트가 있으면 비움")
        void clearExisting() {
            Cart cart = Cart.createFor(memberId);
            cart.addItem(product, sku, 2);

            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.of(cart));

            cartService.clearMyCart(memberId);

            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("카트가 없어도 에러 없음 (멱등성)")
        void clearMissingIsSilent() {
            given(cartRepository.findByMemberIdWithItems(memberId))
                    .willReturn(Optional.empty());

            assertThatCode(() -> cartService.clearMyCart(memberId))
                    .doesNotThrowAnyException();
        }
    }

}