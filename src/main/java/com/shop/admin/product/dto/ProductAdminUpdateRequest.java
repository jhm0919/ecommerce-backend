package com.shop.admin.product.dto;

import jakarta.validation.constraints.Size;

public record ProductAdminUpdateRequest(
        @Size(max = 200) String name,
        int price,
        @Size(max = 4000) String description,
        @Size(max = 500) String mainImageUrl,
        Long categoryId
) {
}
