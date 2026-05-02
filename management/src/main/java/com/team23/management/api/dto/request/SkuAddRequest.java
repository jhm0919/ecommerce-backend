package com.team23.management.api.dto.request;

import com.team23.management.application.command.SkuAddCommand;
import com.team23.management.domain.sku.SkuOptionInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SkuAddRequest(
        @NotNull Long sellerId,
        @NotEmpty List<@Valid SkuOptionInput> options,
        @NotNull Integer additionalPrice,
        @NotNull Integer initialStock
) {
    public SkuAddCommand toCommand(Long productId) {
        return new SkuAddCommand(
                productId, sellerId, options, additionalPrice, initialStock
        );
    }
}
