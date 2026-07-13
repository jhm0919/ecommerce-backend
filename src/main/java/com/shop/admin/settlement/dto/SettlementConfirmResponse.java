package com.shop.admin.settlement.dto;

import java.math.BigDecimal;

public record SettlementConfirmResponse(
        String settledMonth,
        int confirmedCount,
        BigDecimal totalSettlementAmount
) {
}
