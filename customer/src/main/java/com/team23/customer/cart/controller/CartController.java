package com.team23.customer.cart.controller;

import com.team23.customer.cart.dto.AddCartItemRequest;
import com.team23.customer.cart.dto.CartResponse;
import com.team23.customer.cart.dto.UpdateCartItemRequest;
import com.team23.customer.cart.service.CartService;
import com.team23.customer.security.jwt.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 내 장바구니 조회.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        CartService.CartView view = cartService.getMyCart(principal.memberId());
        return ResponseEntity.ok(CartResponse.from(view.cart(), view.productMap()));
    }

    /**
     * 장바구니에 상품 추가.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        CartService.CartView view = cartService.addItem(
                principal.memberId(),
                request.productId(),
                request.skuId(),
                request.quantity()
        );
        return ResponseEntity.ok(CartResponse.from(view.cart(), view.productMap()));
    }

    /**
     * 항목 수량 변경.
     */
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> changeItemQuantity(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartService.CartView view = cartService.changeItemQuantity(
                principal.memberId(),
                itemId,
                request.quantity()
        );
        return ResponseEntity.ok(CartResponse.from(view.cart(), view.productMap()));
    }

    /**
     * 항목 삭제.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long itemId
    ) {
        CartService.CartView view = cartService.removeItem(principal.memberId(), itemId);
        return ResponseEntity.ok(CartResponse.from(view.cart(), view.productMap()));
    }

    /**
     * 장바구니 전체 비우기.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearMyCart(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        cartService.clearMyCart(principal.memberId());
        return ResponseEntity.noContent().build();
    }
}