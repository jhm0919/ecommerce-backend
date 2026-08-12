package com.shop.cart.dto;

import com.shop.cart.domain.Cart;
import com.shop.cart.domain.CartItem;
import com.shop.product.domain.Product;

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
        int totalPrice,
        int itemCount,
        int totalQuantity
) {
    public record CartItemResponse(
            Long itemId,
            Long productId,
            Long skuId, // 추가
            String productName,
            String productImageUrl,
            int currentPrice,
            int quantity,
            int subtotal,
            boolean available
    ) {
        /**
         * Product 정보가 있으면 가격 포함, 없으면 (단종/삭제) 없이 응답.
         */
        public static CartItemResponse from(CartItem item, Product product) {
            if (product == null || product.isVisibleToCustomer()) {
                // 상품이 단종되었거나 삭제됨
                return new CartItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getSkuId(), // ★★★ 2. skuId 값 추가
                        item.getProductName(),  // 스냅샷 사용
                        item.getProductImageUrl(),
                        0,
                        item.getQuantity(),
                        0,
                        false
                );
            }

            int currentPrice = product.getPrice();

            int subtotal = calculateSubtotal(currentPrice, item.getQuantity());

            return new CartItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getSkuId(), // ★★★ 3. skuId 값 추가
                    product.getName(),  // 현재 이름 (스냅샷보다 우선)
                    product.getMainImageUrl(),  // 현재 이미지
                    currentPrice,
                    item.getQuantity(),
                    subtotal,
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
        int totalPrice = calculateTotalPrice(itemResponses);

        return new CartResponse(
                cart.getId(),
                itemResponses,
                totalPrice,
                cart.getItemCount(),
                cart.getTotalQuantity()
        );
    }

    public static int calculateSubtotal(int price, int quantity) {
        return Math.multiplyExact(price, quantity);
    }

    /**
     * 사용 가능한 항목들의 합계 계산.
     */
    private static int calculateTotalPrice(List<CartItemResponse> items) {
        return items.stream()
                .filter(CartItemResponse::available)
                .mapToInt(CartItemResponse::subtotal)
                .reduce(0, Math::addExact);
    }
}