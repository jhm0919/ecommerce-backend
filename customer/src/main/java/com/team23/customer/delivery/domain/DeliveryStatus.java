package com.team23.customer.delivery.domain;

public enum DeliveryStatus {
    PREPARING,     // 배송 준비 중 (주문 직후)
    IN_TRANSIT,    // 배송 중 (운송장 발행 후)
    DELIVERED,     // 배송 완료
    FAILED;        // 배송 실패 (반송 등)

    public boolean isShippable() {
        return this == PREPARING;
    }

    public boolean isCompletable() {
        return this == IN_TRANSIT;
    }

    public boolean isFinalized() {
        return this == DELIVERED || this == FAILED;
    }
}
