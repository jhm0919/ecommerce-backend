package com.team23.customer.order.service;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderAdmin;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.dto.OrderAdminConfirmResponse;
import com.team23.customer.order.dto.OrderAdminListResponse;
import com.team23.customer.order.repository.OrderAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderAdminService {
    private final OrderAdminRepository orderAdminRepository;
    
    @Transactional(readOnly = true)
    public Page<OrderAdminListResponse> search(
            OrderStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to != null) ? to.atTime(23, 59, 59) : null;

        return orderAdminRepository
                .search(status, fromDt, toDt, pageable)
                .map(OrderAdminListResponse::from);
    }

    public OrderAdminConfirmResponse confirm(List<Long> orderIds) {

        List<Order> orders = orderIds.stream()
                .map(id -> orderAdminRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.ORDER_NOT_FOUND) {}))
                .toList();

        // 전체 검증 먼저 — 하나라도 실패 시 전체 롤백
        orders.forEach(order -> {
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
            }
        });

        // 전부 통과 시 일괄 확정
        orders.forEach(OrderAdmin::confirm);

        List<Long> confirmedIds = orders.stream()
                .map(Order::getId)
                .toList();

        return new OrderAdminConfirmResponse(orders.size(), confirmedIds);
    }
}
