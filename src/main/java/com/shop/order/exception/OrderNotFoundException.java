package com.shop.order.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found: id=" + orderId);
    }

    public OrderNotFoundException(String orderNumber) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found: orderNumber=" + orderNumber);
    }
}
