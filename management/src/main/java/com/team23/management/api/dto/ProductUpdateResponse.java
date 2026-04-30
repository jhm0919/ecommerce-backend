package com.team23.management.api.dto;


import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;

public record ProductUpdateResponse(
        Long id,
        String name,
        Category category,
        int basePrice,
        String description,
        ProductStatus status,
        Long sellerId
) {
    public static ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getBasePrice(),
                product.getDescription(),
                product.getStatus(),
                product.getSellerId()
        );
    }
}
