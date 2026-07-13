package com.shop.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddSkuRequest(
        @NotEmpty(message = "옵션은 1개 이상이어야 합니다")
        List<SkuOptionRequest> options,

        @NotNull
        @Min(value = 0, message = "초기 재고는 0 이상이어야 합니다")
        Integer initialStock
) {
    public record SkuOptionRequest(
            @NotNull String name,
            @NotNull String value
    ) {}
}
