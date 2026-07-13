package com.shop.order.dto;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderAdminListResponse(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        OrderStatus status,
        int itemCount,
        LocalDateTime createdAt
) {
    public static OrderAdminListResponse from(Order order) {
        return new OrderAdminListResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount().getAmount(),
                order.getStatus(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }
}
