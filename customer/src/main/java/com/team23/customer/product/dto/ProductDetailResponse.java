package com.team23.customer.product.dto;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 상세 조회 응답.
 * 상세 페이지에 필요한 모든 정보 포함.
 */
public record ProductDetailResponse(
        Long id,
        String name,
        BigDecimal price,
        String currency,
        String description,
        String mainImageUrl,
        CategoryResponse category,
        ProductStatus status,
        boolean inStock,
        LocalDateTime createdAt
) {
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getDescription(),
                product.getMainImageUrl(),
                CategoryResponse.from(product.getCategory()),
                product.getStatus(),
                product.isInStock(),
                product.getCreatedAt()
        );
    }
}
