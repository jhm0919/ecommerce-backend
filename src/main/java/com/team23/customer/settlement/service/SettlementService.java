package com.team23.customer.settlement.service;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.repository.OrderAdminRepository;
import com.team23.customer.settlement.domain.Settlement;
import com.team23.customer.settlement.dto.SettlementConfirmResponse;
import com.team23.customer.settlement.dto.SettlementItem;
import com.team23.customer.settlement.dto.SettlementSummaryResponse;
import com.team23.customer.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {
    private static final double FEE_RATE = 0.035; // 수수료 : 3.5%
    private static final double FEE_RATE_PERCENT = 3.5;

    private final OrderAdminRepository orderAdminRepository;
    private final SettlementRepository settlementRepository;

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
                FEE_RATE_PERCENT,
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

    public SettlementConfirmResponse confirm(YearMonth settledMonth) {
        // 1. 대상 월 CONFIRMED 주문 조회
        String monthStr = settledMonth.toString();
        List<Order> confirmedOrders = orderAdminRepository
                .searchByStatus(
                        OrderStatus.CONFIRMED,
                        settledMonth.atDay(1).atStartOfDay(),
                        settledMonth.atEndOfMonth().atTime(23, 59, 59)
                );

        // 2. 이미 정산된 주문 제외 (중복 방지)
        Set<Long> alreadySettled = settlementRepository.findOrderIdsBySettledMonth(monthStr);

        List<Order> targets = confirmedOrders.stream()
                .filter(o -> !alreadySettled.contains(o.getId()))
                .toList();

        // 3. 정산 대상 없으면 예외
        if (targets.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_SETTLEMENT_TARGET) {};
        }

        // 4. Settlement 생성 + 저장
        List<Settlement> settlements = targets.stream()
                .map(order -> {
                    BigDecimal amount = order.getTotalAmount().getAmount();
                    BigDecimal fee = amount
                            .multiply(BigDecimal.valueOf(FEE_RATE))
                            .setScale(0, RoundingMode.HALF_UP);
                    BigDecimal settlementAmount = amount.subtract(fee);

                    return Settlement.confirm(
                            order.getId(),
                            amount,
                            fee,
                            settlementAmount,
                            settledMonth
                    );
                })
                .toList();

        settlementRepository.saveAll(settlements);

        // 5. 응답 계산
        BigDecimal totalSettlement = settlements.stream()
                .map(Settlement::getSettlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SettlementConfirmResponse(
                monthStr,
                settlements.size(),
                totalSettlement
        );
    }
}
