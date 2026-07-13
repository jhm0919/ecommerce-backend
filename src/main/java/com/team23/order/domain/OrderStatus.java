package com.team23.order.domain;

/**
 * 주문 상태.
 *
 * <p>배송 상태(준비 중, 배송 중, 완료)는 별도 {@code Delivery} Aggregate에서 관리한다.
 * Order는 주문 자체의 라이프사이클(생성됨, 취소됨)만 표현한다.
 *
 * <p>주문이 "완료"되었는지 알고 싶으면 Order와 Delivery를 조합하여 판단:
 * <ul>
 *   <li>Order.PENDING + Delivery.DELIVERED → 사용자 입장 "완료"</li>
 *   <li>Order.CANCELLED → 취소됨</li>
 * </ul>
 */
public enum OrderStatus {
    PENDING,      // 결제 완료 (확정 대기)
    CONFIRMED,    // 판매자 확정 (상품 준비중) ← 추가
    CANCELLED;     // 취소

    public boolean isCancellable() {
        return this == PENDING;
    }
}
