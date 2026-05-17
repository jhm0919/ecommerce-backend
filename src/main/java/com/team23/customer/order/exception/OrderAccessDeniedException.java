package com.team23.customer.order.exception;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;

/**
 * 주문 접근 권한 없음.
 * 보안 상 OrderNotFoundException과 동일하게 404로 응답.
 */
public class OrderAccessDeniedException extends BusinessException {
    public OrderAccessDeniedException() {
        super(ErrorCode.ORDER_ACCESS_DENIED);
    }
}