package com.team23.customer.product.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 상품 수정 요청 (PATCH).
 *
 * <p>모든 필드는 선택. null이면 변경 X.
 */
public record ProductUpdateRequest(
        @Size(max = 200) String name,
        BigDecimal price,
        String currency,
        @Size(max = 4000) String description,
        @Size(max = 500) String mainImageUrl,
        Long categoryId
) {
}
