package com.team23.customer.purchaseorder.exception;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;

public class PurchaseOrderException extends BusinessException {
    public PurchaseOrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PurchaseOrderException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
