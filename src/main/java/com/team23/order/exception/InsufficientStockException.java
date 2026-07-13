package com.team23.order.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
}
