package com.team23.customer.notification.domain;

/**
 * 알림을 만든 원천 데이터.
 *
 * <p>같은 업무 이벤트가 중복 처리되어도 source 기준으로 멱등성을 보장한다.
 */
public enum NotificationSourceType {
    STOCK_HISTORY
}
