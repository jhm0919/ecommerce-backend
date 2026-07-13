package com.team23.order.delivery.repository;

import com.team23.order.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /**
     * 주문 ID로 배송 정보 조회.
     * Order와 Delivery는 1:1이므로 단일 결과.
     */
    Optional<Delivery> findByOrderId(Long orderId);
}
