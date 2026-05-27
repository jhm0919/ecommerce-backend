package com.team23.customer.stockhistory.domain;

/**
 * 재고 이력이 저장된 뒤 발행되는 이벤트.
 *
 * <p>알림은 커밋된 StockHistory를 source로 삼아 생성한다.
 */
public record StockHistoryRecordedEvent(StockHistory stockHistory) {
}
