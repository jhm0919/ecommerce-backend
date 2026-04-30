package com.team23.management.api.dto;

public record SkuAddResponse(
        Long skuId,
        Long productId
) {
    public static SkuAddResponse of(Long skuId, Long productId) {
        return new SkuAddResponse(skuId, productId);
    }
}
