package com.team23.customer.stockhistory.domain;

public enum StockChangeType {
    ORDER,           // 주문으로 인한 차감
    ORDER_CANCEL,    // 주문 취소로 인한 복구
    ADMIN_INCREASE,  // 어드민 수동 증가
    ADMIN_DECREASE,  // 어드민 수동 감소
    SKU_CREATED      // SKU 최초 생성 (초기 재고)
}
