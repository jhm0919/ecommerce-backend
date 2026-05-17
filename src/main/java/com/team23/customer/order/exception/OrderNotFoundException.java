package com.team23.customer.order.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found: id=" + orderId);
    }

    public OrderNotFoundException(String orderNumber) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found: orderNumber=" + orderNumber);
    }
}
