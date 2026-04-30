package com.team23.customer.cart.service;

import com.team23.customer.cart.domain.Cart;
import com.team23.customer.cart.exception.CartItemNotFoundException;
import com.team23.customer.cart.exception.ProductNotPurchasableException;
import com.team23.customer.cart.repository.CartRepository;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.ProductRepository;
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

    // ─────────────────────────────────────
    // 조회
    // ─────────────────────────────────────

    /**
     * 회원의 장바구니 조회.
     * 카트가 없으면 빈 카트 생성하여 반환.
     */
    @Transactional
    public CartView getMyCart(Long memberId) {
        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseGet(() -> cartRepository.save(Cart.createFor(memberId)));

        Map<Long, Product> productMap = loadProductsForCart(cart);
        return new CartView(cart, productMap);
    }

    // ─────────────────────────────────────
    // 항목 추가
    // ─────────────────────────────────────

    /**
     * 장바구니에 상품 추가.
     * 같은 상품이 이미 있으면 수량 합산.
     */
    @Transactional
    public CartView addItem(Long memberId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 단종 상품은 추가 불가
        if (!product.isPurchasable()) {
            throw new ProductNotPurchasableException(productId);
        }

        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseGet(() -> cartRepository.save(Cart.createFor(memberId)));

        cart.addItem(product, quantity);

        log.info("Item added to cart: memberId={}, productId={}, quantity={}",
                memberId, productId, quantity);

        Map<Long, Product> productMap = loadProductsForCart(cart);
        return new CartView(cart, productMap);
    }

    // ─────────────────────────────────────
    // 수량 변경
    // ─────────────────────────────────────

    /**
     * 장바구니 항목의 수량 변경.
     */
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
    // 항목 삭제
    // ─────────────────────────────────────

    /**
     * 장바구니에서 항목 제거.
     */
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
    // 전체 비우기
    // ─────────────────────────────────────

    /**
     * 장바구니 전체 비우기.
     * 카트가 없으면 무동작 (멱등성).
     */
    @Transactional
    public void clearMyCart(Long memberId) {
        cartRepository.findByMemberIdWithItems(memberId)
                .ifPresent(Cart::clear);

        log.info("Cart cleared: memberId={}", memberId);
    }

    // ─────────────────────────────────────
    // 헬퍼 메서드
    // ─────────────────────────────────────

    /**
     * Cart의 모든 항목에 대한 Product를 한 번에 조회 (N+1 방지).
     */
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

    /**
     * Service 내부 전달용 — Cart + Product 매핑을 함께 전달.
     * Controller가 DTO 변환 시 사용.
     */
    public record CartView(Cart cart, Map<Long, Product> productMap) {
    }
}
