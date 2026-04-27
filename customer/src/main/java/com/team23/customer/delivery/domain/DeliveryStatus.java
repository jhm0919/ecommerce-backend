package com.team23.customer.delivery.domain;

public enum DeliveryStatus {
    PREPARING,    // 배송 준비 중
    IN_TRANSIT,   // 배송 중
    DELIVERED,    // 배송 완료
    FAILED        // 배송 실패 (반송 등)
}
