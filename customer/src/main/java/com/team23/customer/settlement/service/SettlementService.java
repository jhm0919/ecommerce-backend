package com.team23.customer.settlement.service;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.repository.OrderAdminRepository;
import com.team23.customer.settlement.dto.SettlementItem;
import com.team23.customer.settlement.dto.SettlementSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {
    private static final double FEE_RATE = 0.035; // 수수료 : 3.5%

    private final OrderAdminRepository orderAdminRepository;

    @Transactional(readOnly = true)
    public SettlementSummaryResponse search(LocalDate from, LocalDate to) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to != null) ? to.atTime(23, 59, 59) : null;

        // CONFIRMED 상태 주문만 조회
        List<Order> confirmedOrders = orderAdminRepository.searchByStatus(OrderStatus.CONFIRMED, fromDt, toDt);

        List<SettlementItem> items = confirmedOrders.stream().map(this::toSettlementItem).toList();

        BigDecimal totalSales = items.stream()
                .map(SettlementItem::orderAmount) // 각 주문의 주문금액만 꺼냄
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 전부 더하기

        BigDecimal totalFee = items.stream()
                .map(SettlementItem::fee) // 각 주문의 수수료만 꺼냄
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSettlement = totalSales.subtract(totalFee);

        return new SettlementSummaryResponse(
                totalSales,
                totalFee,
                totalSettlement,
                FEE_RATE * 100,
                items
        );
    }

    private SettlementItem toSettlementItem(Order order) {
        BigDecimal amount = order.getTotalAmount().getAmount();
        BigDecimal fee = amount.multiply(BigDecimal.valueOf(FEE_RATE))
                .setScale(0, RoundingMode.HALF_UP); // 원 단위 반올림
        BigDecimal settlement = amount.subtract(fee);

        return new SettlementItem(
                order.getId(),
                order.getOrderNumber(),
                amount,
                fee,
                settlement,
                order.getUpdatedAt()   // 확정 시점
        );
    }
}
