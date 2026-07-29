package com.shop.order.dto;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 응답.
 * Order + Delivery + Items 조합.
 */
public record OrderDetailResponse(
        String orderNumber,
        OrderStatus status,
        int totalPrice,
        List<OrderItemResponse> items,
        String zipcode,
        String address,
        String receiverName,
        String receiverPhone,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
    public record OrderItemResponse(
            Long productId,
            String productName,
            int price,
            String productImageUrl,
            int quantity,
            int subtotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getProductId(),
                    item.getProductName(),
                    item.getPrice(),
                    item.getProductImageUrl(),
                    item.getQuantity(),
                    item.calculateSubtotal()
            );
        }
    }

    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getZipcode(),
                order.getAddress(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getMemo(),
                order.getCreatedAt(),
                order.getCancelledAt()
        );
    }
}
