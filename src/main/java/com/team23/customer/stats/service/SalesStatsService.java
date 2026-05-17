package com.team23.customer.stats.service;

import com.team23.customer.order.repository.OrderItemRepository;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.customer.stats.dto.SalesStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesStatsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public SalesStatsResponse getSalesStats(LocalDate from, LocalDate to) {
        // 날짜 → 시간 변환 (from 시작, to 끝)
        LocalDateTime fromDt = from.atStartOfDay();           // 00:00:00
        LocalDateTime toDt = to.atTime(23, 59, 59);          // 23:59:59

        // 1. 매출액 + 주문 건수
        List<Object[]> revenueResult = orderRepository
                .findRevenueAndOrderCount(fromDt, toDt);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long orderCount = 0L;

        if (!revenueResult.isEmpty() && revenueResult.get(0)[0] != null) {
            totalRevenue = (BigDecimal) revenueResult.get(0)[0];
            orderCount = (Long) revenueResult.get(0)[1];
        }

        // 2. 취소 건수
        long cancelCount = orderRepository.countCancelledOrders(fromDt, toDt);

        // 3. 인기 상품 Top 5
        List<SalesStatsResponse.PopularProductResponse> popularProducts =
                orderItemRepository.findTop5PopularProducts(fromDt, toDt)
                        .stream()
                        .map(row -> new SalesStatsResponse.PopularProductResponse(
                                (Long) row[0],
                                (String) row[1],
                                (Long) row[2],
                                (BigDecimal) row[3]
                        ))
                        .toList();

        return new SalesStatsResponse(
                totalRevenue,
                "KRW",
                orderCount,
                cancelCount,
                popularProducts
        );
    }
}
