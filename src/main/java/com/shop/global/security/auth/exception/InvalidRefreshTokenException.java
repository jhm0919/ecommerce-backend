package com.shop.global.security.auth.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
