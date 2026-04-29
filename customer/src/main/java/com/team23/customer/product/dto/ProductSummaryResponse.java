package com.team23.customer.product.dto;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;

import java.math.BigDecimal;

/**
 * 상품 목록 조회 응답.
 * 목록에 필요한 최소 정보만 포함 (성능 최적화).
 */
public record ProductSummaryResponse(
        Long id,
        String name,
        BigDecimal price,
        String currency,
        String mainImageUrl,
        String categoryName,
        ProductStatus status,
        boolean inStock
) {
    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getMainImageUrl(),
                product.getCategory().getName(),
                product.getStatus(),
                product.isInStock()
        );
    }
}