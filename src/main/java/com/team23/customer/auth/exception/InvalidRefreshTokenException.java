package com.team23.customer.auth.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
