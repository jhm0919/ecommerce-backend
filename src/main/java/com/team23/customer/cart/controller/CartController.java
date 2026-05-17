package com.team23.customer.cart.controller;

import com.team23.customer.cart.dto.AddCartItemRequest;
import com.team23.customer.cart.dto.CartResponse;
import com.team23.customer.cart.dto.UpdateCartItemRequest;
import com.team23.customer.cart.service.CartService;
import com.team23.common.response.CommonResponse;
import com.team23.common.security.jwt.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장바구니", description = "회원 장바구니 API")
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "내 장바구니 조회", description = "로그인한 회원의 장바구니를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<CartResponse>> getMyCart(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        CartService.CartView view = cartService.getMyCart(principal.memberId());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(CartResponse.from(view.cart(), view.productMap()))
        );
    }

    @Operation(summary = "장바구니 상품 추가", description = "장바구니에 상품(SKU)을 추가한다. 같은 SKU면 수량 합산.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "재고 부족 / 입력값 오류"),
            @ApiResponse(responseCode = "404", description = "상품 또는 SKU 없음")
    })
    @PostMapping("/items")
    public ResponseEntity<CommonResponse<CartResponse>> addItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        CartService.CartView view = cartService.addItem(
                principal.memberId(),
                request.productId(),
                request.skuId(),
                request.quantity()
        );
        return ResponseEntity.ok(
                CommonResponse.createSuccess(CartResponse.from(view.cart(), view.productMap()))
        );
    }

    @Operation(summary = "장바구니 수량 변경", description = "장바구니 항목의 수량을 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "재고 부족 / 입력값 오류"),
            @ApiResponse(responseCode = "404", description = "항목 없음")
    })
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CommonResponse<CartResponse>> changeItemQuantity(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartService.CartView view = cartService.changeItemQuantity(
                principal.memberId(),
                itemId,
                request.quantity()
        );
        return ResponseEntity.ok(
                CommonResponse.createSuccess(CartResponse.from(view.cart(), view.productMap()))
        );
    }

    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 특정 항목을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "항목 없음")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CommonResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long itemId
    ) {
        CartService.CartView view = cartService.removeItem(principal.memberId(), itemId);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(CartResponse.from(view.cart(), view.productMap()))
        );
    }

    @Operation(summary = "장바구니 전체 비우기", description = "장바구니의 모든 항목을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "비우기 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping
    public ResponseEntity<Void> clearMyCart(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        cartService.clearMyCart(principal.memberId());
        return ResponseEntity.noContent().build();
    }
}