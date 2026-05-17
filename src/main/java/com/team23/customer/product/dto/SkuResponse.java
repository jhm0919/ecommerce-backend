package com.team23.customer.product.dto;

import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.domain.SkuOption;

import java.util.List;

public record SkuResponse(
        Long skuId,
        String skuCode,
        List<SkuOptionResponse> options,
        int stock
) {
    public record SkuOptionResponse(
            String name,
            String value
    ) {
        public static SkuOptionResponse from(SkuOption option) {
            return new SkuOptionResponse(option.getOptionName(), option.getOptionValue());
        }
    }

    public static SkuResponse from(SKU sku) {
        return new SkuResponse(
                sku.getId(),
                sku.getSkuCode(),
                sku.getOptions().stream()
                        .map(SkuOptionResponse::from)
                        .toList(),
                sku.getStock()
        );
    }
}
