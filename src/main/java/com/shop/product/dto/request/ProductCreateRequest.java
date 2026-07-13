package com.shop.product.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 상품 등록 요청.
 */
public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다")
        String name,

        @NotNull(message = "가격은 필수입니다")
        @DecimalMin(value = "0", inclusive = true, message = "가격은 0 이상이어야 합니다")
        BigDecimal price,

//        @NotNull(message = "통화는 필수입니다")
//        @Size(min = 3, max = 3, message = "통화 코드는 3자입니다")
//        String currency,

        @Size(max = 4000, message = "설명은 4000자를 초과할 수 없습니다")
        String description,

        @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
        String mainImageUrl,

        @NotNull(message = "카테고리 ID는 필수입니다")
        Long categoryId
) {
}