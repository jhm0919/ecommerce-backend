package com.team23.management.api.dto;

import com.team23.management.application.command.ProductUpdateCommand;
import com.team23.management.domain.product.Category;
import jakarta.validation.constraints.NotNull;

public record ProductUpdateRequest(
        @NotNull
        Long sellerId,
        String name,           // 선택 — null 가능
        Category category,     // 선택
        Integer basePrice,     // 선택
        String description     // 선택
) {
    public ProductUpdateCommand toCommand(Long productId) {
        return new ProductUpdateCommand(
                productId, sellerId, name, category, basePrice, description
        );
    }
}
