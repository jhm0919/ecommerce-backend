package com.shop.product.dto;

import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;

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

    public static SkuResponse from(Sku sku) {
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
