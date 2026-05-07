package com.team23.customer.order.domain;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;

public class OrderAdmin {

    public static void confirm(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        order.updateStatus(OrderStatus.CONFIRMED);
    }

    public static void forceCancel(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        order.updateStatus(OrderStatus.CANCELLED);
    }
}
