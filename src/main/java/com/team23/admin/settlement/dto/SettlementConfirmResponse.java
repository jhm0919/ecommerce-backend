package com.team23.admin.settlement.dto;

import java.math.BigDecimal;

public record SettlementConfirmResponse(
        String settledMonth,
        int confirmedCount,
        BigDecimal totalSettlementAmount
) {
}
