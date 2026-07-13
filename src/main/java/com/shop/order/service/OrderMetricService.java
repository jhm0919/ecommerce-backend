package com.shop.order.service;

import com.shop.order.domain.OrderStatus;
import com.shop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderMetricService {
    private final OrderRepository orderRepository;

    public long getPendingOrderCount() {
        return orderRepository.countByStatus(OrderStatus.PENDING);
    }

    public long getConfirmedOrderCount() {
        return orderRepository.countByStatus(OrderStatus.CONFIRMED);
    }

    public long getCancelledOrderCount() {
        return orderRepository.countByStatus(OrderStatus.CANCELLED);
    }
}
