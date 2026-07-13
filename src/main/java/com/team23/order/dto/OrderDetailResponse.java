package com.team23.order.dto;

import com.team23.order.delivery.domain.Delivery;
import com.team23.order.delivery.domain.DeliveryStatus;
import com.team23.order.domain.Order;
import com.team23.order.domain.OrderItem;
import com.team23.order.domain.OrderStatus;

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
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        DeliveryResponse delivery,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
    public record OrderItemResponse(
            Long productId,
            String productName,
            BigDecimal priceAmount,
            String productImageUrl,
            int quantity,
            BigDecimal subtotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getProductId(),
                    item.getProductName(),
                    item.getPriceAtOrder().getAmount(),
                    item.getProductImageUrl(),
                    item.getQuantity(),
                    item.calculateSubtotal().getAmount()
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
                order.getTotalAmount().getAmount(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                DeliveryResponse.from(delivery),
                order.getCreatedAt(),
                order.getCancelledAt()
        );
    }
}
