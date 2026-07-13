package com.shop.product.dto.response;

import com.shop.product.domain.Product;
import com.shop.product.domain.ProductStatus;
import com.shop.product.dto.ProductSummaryProjection;

import java.math.BigDecimal;

/**
 * 상품 목록 조회 응답.
 * 목록에 필요한 최소 정보만 포함 (성능 최적화).
 * 재고는 모든 SKU 합계로 표시된다.
 */
public record ProductListResponse(
        Long id,
        String name,
        BigDecimal price,
//        String currency,
        String mainImageUrl,
        String categoryName,
        ProductStatus status,
        boolean inStock,
        int totalStock  // ★ SKU 합계 재고
) {
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getMainImageUrl(),
                product.getCategory().getName(),
                product.getStatus(),
                product.isInStock(),              // getTotalSkuStock() > 0
                product.getTotalSkuStock()        // ★ SKU 합계
        );
    }

    public static ProductListResponse from(ProductSummaryProjection projection) {
        int totalStock = toSafeTotalStock(projection.totalStock());

        return new ProductListResponse(
                projection.id(),
                projection.name(),
                projection.price().getAmount(),
                projection.mainImageUrl(),
                projection.categoryName(),
                projection.status(),
                totalStock > 0,
                totalStock
        );
    }

    private static int toSafeTotalStock(Long totalStock) {
        long rawTotalStock = totalStock == null ? 0L : totalStock;
        if (rawTotalStock <= 0L) {
            return 0;
        }
        if (rawTotalStock > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) rawTotalStock;
    }
}
