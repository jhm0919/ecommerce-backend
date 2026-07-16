package com.shop.admin.sales.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class InvalidMonthException extends BusinessException {
    public InvalidMonthException() {
        super(ErrorCode.INVALID_SALES_MONTH);
    }
}
