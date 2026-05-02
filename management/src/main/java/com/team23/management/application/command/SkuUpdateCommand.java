package com.team23.management.application.command;

public record SkuUpdateCommand(
        Long productId,
        Long skuId,
        Long sellerId,
        Integer additionalPrice
) {
}
