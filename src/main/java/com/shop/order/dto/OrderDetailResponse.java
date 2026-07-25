package com.shop.order.dto;

import com.shop.order.delivery.domain.Delivery;
import com.shop.order.delivery.domain.DeliveryStatus;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.domain.OrderStatus;

import java.math.BigDecimal;
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
        DeliveryResponse delivery,
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

    public record DeliveryResponse(
            DeliveryStatus status,
            String receiverName,
            String receiverPhone,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String memo,
            String trackingNumber,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt
    ) {
        public static DeliveryResponse from(Delivery delivery) {
            return new DeliveryResponse(
                    delivery.getStatus(),
                    delivery.getReceiver().getName(),
                    delivery.getReceiver().getPhone(),
                    delivery.getAddress().getZipCode(),
                    delivery.getAddress().getAddressLine1(),
                    delivery.getAddress().getAddressLine2(),
                    delivery.getMemo(),
                    delivery.getTrackingNumber(),
                    delivery.getShippedAt(),
                    delivery.getDeliveredAt()
            );
        }
    }

    public static OrderDetailResponse from(Order order, Delivery delivery) {
        return new OrderDetailResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                DeliveryResponse.from(delivery),
                order.getCreatedAt(),
                order.getCancelledAt()
        );
    }
}
