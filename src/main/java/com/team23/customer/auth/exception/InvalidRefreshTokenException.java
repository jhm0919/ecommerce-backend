package com.team23.customer.auth.exception;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
