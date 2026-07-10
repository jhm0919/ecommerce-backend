package com.team23.customer.category.dto;

import com.team23.customer.category.domain.Category;

/**
 * 카테고리 정보 응답.
 */
public record CategoryResponse(
        Long id,
        String name,
        String slug
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }
}