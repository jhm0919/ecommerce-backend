package com.shop.admin.order.domain;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.order.domain.OrderStatus;

public class OrderAdmin {

    public static void confirm(com.shop.order.domain.Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        order.updateStatus(OrderStatus.CONFIRMED);
    }

    public static void forceCancel(com.shop.order.domain.Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
        }
        order.updateStatus(OrderStatus.CANCELLED);
    }
}
