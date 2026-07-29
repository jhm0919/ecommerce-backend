package com.shop.order.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(int quantity, int reqQuantity) {
        super(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock. Current: " + quantity + ", requested: " + reqQuantity);
    }
}
