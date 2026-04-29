package com.team23.management.api.dto;

import com.team23.management.application.command.ProductRegisterCommand;
import com.team23.management.application.command.SkuCommand;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.product.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductRegisterRequest(
        @NotBlank
        String name,

        @NotNull
        Category category,

        @NotNull
        Integer basePrice,

        String description,

        @NotNull
        Long sellerId,

        @NotEmpty
        List<@Valid SkuRequest> skus
) {
    public record SkuRequest(
            @NotEmpty
            List<SkuOptionInput> options,

            @NotNull
            Integer additionalPrice,

            @NotNull
            Integer initialStock
    ) {}

    // Command 변환
    public ProductRegisterCommand toCommand() {
        return new ProductRegisterCommand(
                name,
                category,
                basePrice,
                description,
                sellerId,
                skus.stream()
                        .map(s -> new SkuCommand(
                                s.options(), s.additionalPrice(), s.initialStock()
                        ))
                        .toList()
        );
    }
}
