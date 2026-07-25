package com.shop.admin.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductAdminCreateRequest(
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다")
        String name,

        @NotNull(message = "가격은 필수입니다")
        @DecimalMin(value = "0", inclusive = true, message = "가격은 0 이상이어야 합니다")
        int price,

        @Size(max = 4000, message = "설명은 4000자를 초과할 수 없습니다")
        String description,

        @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
        String mainImageUrl,

        @NotNull(message = "카테고리 ID는 필수입니다")
        Long categoryId
) {
}
