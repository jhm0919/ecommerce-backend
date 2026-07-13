package com.shop.admin.purchaseorder.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class PurchaseOrderException extends BusinessException {
    public PurchaseOrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PurchaseOrderException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
