package com.team23.management.api.dto.request;

import com.team23.management.application.command.SkuUpdateCommand;
import jakarta.validation.constraints.NotNull;

public record SkuUpdateRequest(
        @NotNull Long sellerId,
        @NotNull Integer additionalPrice
) {
    public SkuUpdateCommand toCommand(Long productId, Long skuId) {
        return new SkuUpdateCommand(productId, skuId, sellerId, additionalPrice);
    }
}
