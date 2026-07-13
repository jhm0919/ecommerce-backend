package com.team23.order.dto;

import com.team23.order.domain.Order;
import com.team23.order.domain.OrderStatus;

public record OrderAdminCancelResponse(
        Long orderId,
        String orderNumber,
        OrderStatus status,
        String cancelReason
) {
    public static OrderAdminCancelResponse from(Order order, String cancelReason) {
        return new OrderAdminCancelResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                cancelReason
        );
    }
}
