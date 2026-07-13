package com.team23.cart.dto;

import com.team23.cart.domain.Cart;
import com.team23.cart.domain.CartItem;
import com.team23.product.domain.Money;
import com.team23.product.domain.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 장바구니 조회 응답.
 *
 * <p>각 항목은 현재 Product의 가격으로 계산된다.
 * 단종된 상품은 {@code available=false}로 표시되며 totalAmount 계산에서 제외된다.
 */
public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
//        String currency,
        int itemCount,
        int totalQuantity
) {
    public record CartItemResponse(
            Long itemId,
            Long productId,
            Long skuId, // 추가
            String productName,
            String productImageUrl,
            BigDecimal currentPrice,
//            String currency,
            int quantity,
            BigDecimal subtotal,
            boolean available
    ) {
        /**
         * Product 정보가 있으면 가격 포함, 없으면 (단종/삭제) 없이 응답.
         */
        public static CartItemResponse from(CartItem item, Product product) {
            if (product == null || !product.isVisibleToCustomer()) {
                // 상품이 단종되었거나 삭제됨
                return new CartItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getSkuId(), // ★★★ 2. skuId 값 추가
                        item.getProductName(),  // 스냅샷 사용
                        item.getProductImageUrl(),
                        null,
//                        null,
                        item.getQuantity(),
                        null,
                        false
                );
            }

            Money currentPrice = product.getPrice();
            Money subtotal = currentPrice.multiply(item.getQuantity());

            return new CartItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getSkuId(), // ★★★ 3. skuId 값 추가
                    product.getName(),  // 현재 이름 (스냅샷보다 우선)
                    product.getMainImageUrl(),  // 현재 이미지
                    currentPrice.getAmount(),
//                    currentPrice.getCurrency(),
                    item.getQuantity(),
                    subtotal.getAmount(),
                    true
            );
        }
    }

    /**
     * Cart와 Product 정보를 조합하여 응답을 생성.
     *
     * @param cart 장바구니
     * @param productMap productId → Product 매핑 (한 번에 조회한 결과)
     */
    public static CartResponse from(Cart cart, Map<Long, Product> productMap) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.from(item, productMap.get(item.getProductId())))
                .toList();

        // 사용 가능한 항목들의 합계 계산
        Money total = calculateTotal(itemResponses);

        return new CartResponse(
                cart.getId(),
                itemResponses,
                total != null ? total.getAmount() : BigDecimal.ZERO,
//                total != null ? total.getCurrency() : "KRW",  // 기본 통화
                cart.getItemCount(),
                cart.getTotalQuantity()
        );
    }

    /**
     * 사용 가능한 항목들의 합계 계산.
     */
    private static Money calculateTotal(List<CartItemResponse> items) {
        Money total = null;
        for (CartItemResponse item : items) {
            if (!item.available()) continue;

            Money subtotal = new Money(item.subtotal());
            total = (total == null) ? subtotal : total.add(subtotal);
        }
        return total;
    }
}