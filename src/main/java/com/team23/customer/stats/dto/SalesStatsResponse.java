package com.team23.customer.stats.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 판매 실적 통계 응답.
 */
public record SalesStatsResponse(
        BigDecimal totalRevenue,       // 총 매출액
        String currency,               // 통화
        long orderCount,               // 주문 건수
        long cancelCount,              // 취소 건수
        List<PopularProductResponse> popularProducts  // 인기 상품 Top 5
) {
    public record PopularProductResponse(
            Long productId,
            String productName,
            long totalSoldQuantity,    // 총 판매 수량
            BigDecimal totalRevenue    // 해당 상품 매출
    ) {}
}
