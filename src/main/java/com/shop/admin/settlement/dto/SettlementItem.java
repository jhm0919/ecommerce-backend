package com.shop.admin.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementItem( // 주문별 정산 상세
        Long orderId,
        String orderNumber,
        BigDecimal orderAmount,
        BigDecimal fee,
        BigDecimal settlementAmount,
        LocalDateTime confirmedAt
) {
}
