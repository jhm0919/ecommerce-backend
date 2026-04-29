package com.team23.management.application.command;

import com.team23.management.domain.product.Category;

import java.util.List;

public record ProductRegisterCommand(
        String name,
        Category category,
        int basePrice,
        String description,
        Long sellerId,
        List<SkuCommand> skus
) { }
