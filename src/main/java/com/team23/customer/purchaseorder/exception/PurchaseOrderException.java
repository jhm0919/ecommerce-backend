package com.team23.customer.purchaseorder.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class PurchaseOrderException extends BusinessException {
    public PurchaseOrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PurchaseOrderException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
