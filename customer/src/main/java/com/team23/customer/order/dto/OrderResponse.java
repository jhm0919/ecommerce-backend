package com.team23.customer.order.dto;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 목록 응답 (간단).
 * 상세 정보는 OrderDetailResponse 사용.
 */
public record OrderResponse(
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        int itemCount,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount().getAmount(),
                order.getTotalAmount().getCurrency(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }
}
