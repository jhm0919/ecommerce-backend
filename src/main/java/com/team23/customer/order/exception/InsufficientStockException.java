package com.team23.customer.order.exception;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
}
