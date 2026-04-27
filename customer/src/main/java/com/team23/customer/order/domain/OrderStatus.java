package com.team23.customer.order.domain;

public enum OrderStatus {
    PENDING,      // 주문 생성 (결제 대기)
    PAID,         // 결제 완료
    PREPARING,    // 상품 준비 중
    SHIPPED,      // 배송 시작
    DELIVERED,    // 배송 완료
    CANCELLED,    // 취소됨
    REFUNDED;     // 환불됨

    public boolean canBeCancelled() {
        return this == PENDING || this == PAID || this == PREPARING;
    }

    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }
}
