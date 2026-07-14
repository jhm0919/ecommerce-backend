package com.shop.admin.order.dto;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;

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
