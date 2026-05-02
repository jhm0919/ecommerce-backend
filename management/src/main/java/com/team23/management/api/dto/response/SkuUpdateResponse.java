package com.team23.management.api.dto.response;

import com.team23.management.domain.sku.Sku;

public record SkuUpdateResponse(
        Long skuId,
        int additionalPrice
) {
    public static SkuUpdateResponse from(Sku sku) {
        return new SkuUpdateResponse(sku.getId(), sku.getAdditionalPrice());
    }
}
