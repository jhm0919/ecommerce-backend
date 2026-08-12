package com.shop.cart.service;

import com.shop.cart.domain.Cart;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductNotPurchasableException;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.repository.CategoryRepository;
import com.shop.member.domain.AuthProvider;
import com.shop.member.domain.Member;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    Long memberId;
    Long productId;
    Long skuId;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        memberRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Member member = Member.registerFromOAuth(AuthProvider.GOOGLE, "google", "jhm0919@naver.com", "장해민", "pic_url");
        memberRepository.saveAndFlush(member);
        memberId = member.getId();

        Category category = categoryRepository.saveAndFlush(Category.create("의류", "clothing"));
        
        Product product = productRepository.saveAndFlush(Product.register(
                "티셔츠", 29900, "설명", "img", category
        ));
        productId = product.getId();

        product.addSku(List.of(new SkuOption("색상", "검정")), 50);
        Product savedSkuProduct = productRepository.saveAndFlush(product);
        skuId = savedSkuProduct.getSkuses().get(0).getId();
    }

    @Nested
    @DisplayName("내 장바구니 조회")
    class GetMyCart {

        @Test
        @DisplayName("기존 카트가 있으면 그대로 반환")
        void returnsExistingCart() {
            Cart existingCart = Cart.createFor(memberId);
            cartRepository.saveAndFlush(existingCart);

            CartResponse response = cartService.getMyCart(memberId);

            assertThat(response.cartId()).isEqualTo(existingCart.getId());
        }

        @Test
        @DisplayName("카트가 없으면 새로 생성")
        void createsNewCart() {
            CartResponse response = cartService.getMyCart(memberId);

            Cart cart = cartRepository.findByIdWithItems(response.cartId()).orElseThrow();

            assertThat(cart.getMemberId()).isEqualTo(memberId);
            assertThat(cart.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("상품 추가")
    class AddItem {

        @Test
        @DisplayName("정상적으로 상품 추가")
        void addNormal() {
            CartResponse response = cartService.addItem(memberId, productId, skuId, 2);

            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.totalQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("같은 SKU 다시 추가 시 합산")
        void mergeSameSku() {
            cartService.addItem(memberId, productId, skuId, 2); // 미리 2개 담음

            CartResponse response = cartService.addItem(memberId, productId, skuId, 3);

            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.totalQuantity()).isEqualTo(5);  // 2 + 3
        }

        @Test
        @DisplayName("존재하지 않는 상품은 ProductNotFoundException")
        void rejectUnknownProduct() {

            assertThatThrownBy(() -> cartService.addItem(memberId, 999L, skuId, 1))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("단종 상품은 ProductNotPurchasableException")
        void rejectDiscontinued() {
            Product product = productRepository.findById(productId).orElseThrow();

            product.discontinue();
            productRepository.save(product);

            assertThatThrownBy(() -> cartService.addItem(memberId, productId, skuId, 1))
                    .isInstanceOf(ProductNotPurchasableException.class);
        }

        @Test
        @DisplayName("카트가 없으면 자동 생성 후 추가")
        void createsCartIfMissing() {
            CartResponse response = cartService.addItem(memberId, productId, skuId, 1);

            assertThat(response.itemCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("수량 변경")
    class ChangeQuantity {

        @Test
        @DisplayName("정상적으로 수량 변경")
        void changeNormal() {
            CartResponse addedCart = cartService.addItem(memberId, productId, skuId, 2);

            Long itemId = addedCart.items().get(0).itemId();

            CartResponse response = cartService.changeItemQuantity(memberId, itemId, 5);

            assertThat(response.totalQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 항목은 CartItemNotFoundException")
        void rejectUnknownItem() {
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
            CartResponse addedCart = cartService.addItem(memberId, productId, skuId, 2);

            Long itemId = addedCart.items().get(0).itemId();

            CartResponse response = cartService.removeItem(memberId, itemId);

            Cart cart = cartRepository.findByIdWithItems(response.cartId()).orElseThrow();

            assertThat(cart.getMemberId()).isEqualTo(memberId);
            assertThat(cart.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("전체 비우기")
    class ClearCart {

        @Test
        @DisplayName("카트가 있으면 비움")
        void clearExisting() {
            cartService.addItem(memberId, productId, skuId, 2);

            cartService.clearMyCart(memberId);

            Cart cart = cartRepository.findByMemberIdWithItems(memberId).orElseThrow();

            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("카트가 없어도 에러 없음 (멱등성)")
        void clearMissingIsSilent() {
            assertThatCode(() -> cartService.clearMyCart(memberId))
                    .doesNotThrowAnyException();
        }
    }

}