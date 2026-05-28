package com.team23.customer.product.dto;

import com.team23.customer.product.domain.ProductStatus;

import java.math.BigDecimal;

public record ProductSummaryProjection(
        Long id,
        String name,
        BigDecimal price,
        String currency,
        String mainImageUrl,
        String categoryName,
        ProductStatus status,
        Long totalStock
) {
}
