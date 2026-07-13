package com.shop.admin.product.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @Size(max = 200) String name,
        BigDecimal price,
        @Size(max = 4000) String description,
        @Size(max = 500) String mainImageUrl,
        Long categoryId
) {
}
