package com.shop.admin.settlement.dto;

import java.math.BigDecimal;
import java.util.List;

public record SettlementSummaryResponse( // 전체 합계 + 상세
        BigDecimal totalSalesAmount,
        BigDecimal totalFee,
        BigDecimal totalSettlementAmount,
        double feeRate,
        List<SettlementItem> items
) {
}
