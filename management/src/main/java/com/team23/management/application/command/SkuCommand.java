package com.team23.management.application.command;

import com.team23.management.domain.sku.SkuOptionInput;

import java.util.List;

public record SkuCommand(
        List<SkuOptionInput> options,
        int additionalPrice,
        int initialStock
) {}
