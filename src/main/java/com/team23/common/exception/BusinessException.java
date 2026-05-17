package com.team23.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외의 루트 클래스.
 *
 * <p>detail은 <b>서버 로깅용</b>으로만 사용되며, 클라이언트 응답에는 포함되지 않습니다.
 * 민감정보(내부 식별자 등)를 detail로 전달해도 안전합니다.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    protected BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
