package com.shop.product.domain;

import com.shop.admin.stockhistory.domain.StockChangeType;

/**
 * 재고 변동 이벤트.
 * 모든 재고 변동 시 발행된다.
 */
public record StockChangedEvent(
        Long productId,
        String productName,
        Long skuId,
        String skuCode,
        String skuOptionsSnapshot,
        StockChangeType changeType,
        int quantity,       // 변동량 (항상 양수, 방향은 changeType으로)
        int stockBefore,    // 변동 전 재고
        int stockAfter,     // 변동 후 재고
        Long orderId        // 주문 관련 변동 시만 (nullable)
) {
    public static StockChangedEvent of(
            Product product,
            Sku sku,
            StockChangeType changeType,
            int quantity,
            int stockBefore,
            int stockAfter,
            Long orderId
    ) {
        String optionsSnapshot = sku.getOptions().stream()
                .map(opt -> opt.getOptionName() + "=" + opt.getOptionValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return new StockChangedEvent(
                product.getId(),
                product.getName(),
                sku.getId(),
                sku.getSkuCode(),
                optionsSnapshot,
                changeType,
                quantity,
                stockBefore,
                stockAfter,
                orderId
        );
    }
}
