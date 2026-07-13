package com.team23.product.dto;

import com.team23.product.domain.Money;
import com.team23.product.domain.ProductStatus;

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
