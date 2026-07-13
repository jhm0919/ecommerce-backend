package com.shop.order.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
}
