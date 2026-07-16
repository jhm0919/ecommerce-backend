package com.shop.admin.sales.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class InvalidDateException extends BusinessException {
    public InvalidDateException() {
        super(ErrorCode.INVALID_SALES_DATE);
    }
}
