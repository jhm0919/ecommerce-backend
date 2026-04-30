package com.team23.management.api.dto;

import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;

public record ProductListResponse(
        Long id,
        String name,
        Category category,
        int basePrice,
        ProductStatus status
) {
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getBasePrice(),
                product.getStatus()
        );
    }
}
