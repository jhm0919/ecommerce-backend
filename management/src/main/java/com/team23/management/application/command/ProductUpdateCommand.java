package com.team23.management.application.command;

import com.team23.management.domain.product.Category;

public record ProductUpdateCommand(
        Long productId,
        Long sellerId,
        String name,         // 선택 — null 이면 변경 안 함
        Category category,   // 선택
        Integer basePrice,   // 선택
        String description   // 선택
) {
}
