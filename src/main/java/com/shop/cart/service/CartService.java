package com.shop.cart.service;

import com.shop.cart.domain.Cart;
import com.shop.cart.exception.CartItemNotFoundException;
import com.shop.cart.exception.ProductNotPurchasableException;
import com.shop.cart.repository.CartRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
//    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행기 주입

    // ─────────────────────────────────────
    // 조회 (변경 없음)
    // ─────────────────────────────────────

    @Timed(value = "cart.get.time", description = "장바구니 조회 처리 시간")
    @Transactional
    public CartView getMyCart(Long memberId) {
        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseGet(() -> cartRepository.save(Cart.createFor(memberId)));

        Map<Long, Product> productMap = loadProductsForCart(cart);
        return new CartView(cart, productMap);
    }

    // ─────────────────────────────────────
    // 항목 추가 (핵심 변경)
    // ─────────────────────────────────────

    /**
     * 장바구니에 상품 추가.
     * 같은 SKU가 이미 있으면 수량 합산.
     * 같은 Product라도 SKU가 다르면 별도 항목으로 추가.
     */
    @Timed(value = "cart.add.time", description = "장바구니 상품 추가 처리 시간")
    @Transactional
    public CartView addItem(Long memberId, Long productId, Long skuId, int quantity) {  // ★ skuId 추가
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // ★ SKU 조회 + 검증 (이 Product의 SKU인지 — Aggregate 경계 보호)
        Sku sku = product.findSkuById(skuId);

        // 단종 상품은 추가 불가
        if (!product.isPurchasable()) {
            throw new ProductNotPurchasableException(productId);
        }


        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseGet(() -> cartRepository.save(Cart.createFor(memberId)));

        cart.addItem(product, sku, quantity);  // ★ SKU 전달

        log.info("Item added to cart: memberId={}, productId={}, skuId={}, quantity={}",
                memberId, productId, skuId, quantity);

        Map<Long, Product> productMap = loadProductsForCart(cart);

        return new CartView(cart, productMap);
    }

    // ─────────────────────────────────────
    // 수량 변경 (변경 없음)
    // ─────────────────────────────────────

    @Timed(value = "cart.change_quantity.time", description = "장바구니 수량 변경 처리 시간")
    @Transactional
    public CartView changeItemQuantity(Long memberId, Long itemId, int quantity) {
        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        try {
            cart.changeItemQuantity(itemId, quantity);
        } catch (IllegalArgumentException e) {
            log.warn("Cart item not found: memberId={}, itemId={}", memberId, itemId);
            throw new CartItemNotFoundException(itemId);
        }

        log.info("Item quantity changed: memberId={}, itemId={}, quantity={}",
                memberId, itemId, quantity);

        Map<Long, Product> productMap = loadProductsForCart(cart);
        return new CartView(cart, productMap);
    }

    // ─────────────────────────────────────
    // 항목 삭제 (변경 없음)
    // ─────────────────────────────────────

    @Timed(value = "cart.remove.time", description = "장바구니 상품 삭제 처리 시간")
    @Transactional
    public CartView removeItem(Long memberId, Long itemId) {
        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        try {
            cart.removeItem(itemId);
        } catch (IllegalArgumentException e) {
            throw new CartItemNotFoundException(itemId);
        }

        log.info("Item removed from cart: memberId={}, itemId={}", memberId, itemId);

        Map<Long, Product> productMap = loadProductsForCart(cart);
        return new CartView(cart, productMap);
    }

    // ─────────────────────────────────────
    // 전체 비우기 (변경 없음)
    // ─────────────────────────────────────

    @Timed(value = "cart.clear.time", description = "장바구니 비우기 처리 시간")
    @Transactional
    public void clearMyCart(Long memberId) {
        cartRepository.findByMemberIdWithItems(memberId)
                .ifPresent(Cart::clear);

        log.info("Cart cleared: memberId={}", memberId);
    }

    // ─────────────────────────────────────
    // 헬퍼 메서드 (변경 없음)
    // ─────────────────────────────────────

    private Map<Long, Product> loadProductsForCart(Cart cart) {
        if (cart.isEmpty()) {
            return Map.of();
        }

        List<Long> productIds = cart.getItems().stream()
                .map(item -> item.getProductId())
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        return products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    public record CartView(Cart cart, Map<Long, Product> productMap) {}
}