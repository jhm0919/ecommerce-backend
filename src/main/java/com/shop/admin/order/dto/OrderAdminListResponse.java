package com.shop.admin.order.dto;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderAdminListResponse(
        Long orderId,
        String orderNumber,
        int totalPrice,
        OrderStatus status,
        int itemCount,
        LocalDateTime createdAt
) {
    public static OrderAdminListResponse from(Order order) {
        return new OrderAdminListResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }
}
