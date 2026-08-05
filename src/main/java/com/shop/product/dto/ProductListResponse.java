package com.shop.product.dto;

import com.shop.product.domain.Product;
import com.shop.product.domain.ProductStatus;

/**
 * 상품 목록 조회 응답.
 * 목록에 필요한 최소 정보만 포함.
 */
public record ProductListResponse(
        Long id,
        String name,
        int price,
        String mainImageUrl,
        String categoryName,
        ProductStatus status
) {
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getMainImageUrl(),
                product.getCategory().getName(),
                product.getStatus()
        );
    }
}
