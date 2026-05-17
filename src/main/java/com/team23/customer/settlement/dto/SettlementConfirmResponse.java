package com.team23.customer.settlement.dto;

import java.math.BigDecimal;

public record SettlementConfirmResponse(
        String settledMonth,
        int confirmedCount,
        BigDecimal totalSettlementAmount
) {
}
