package com.shop.product.dto;

import com.shop.product.domain.Money;
import com.shop.product.domain.ProductStatus;

public record ProductSummaryProjection(
        Long id,
        String name,
        Money price,
        String mainImageUrl,
        String categoryName,
        ProductStatus status,
        Long totalStock
) {
}
