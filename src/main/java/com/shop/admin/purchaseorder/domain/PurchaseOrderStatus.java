package com.shop.admin.purchaseorder.domain;

/**
 * 발주 상태.
 *
 * REQUESTED → RECEIVED (입고 완료)
 * REQUESTED → CANCELLED (취소)
 */
public enum PurchaseOrderStatus {
    REQUESTED,   // 발주 요청 (입고 대기)
    RECEIVED,    // 입고 완료
    CANCELLED;   // 취소

    public boolean isCancellable() {
        return this == REQUESTED;
    }

}
