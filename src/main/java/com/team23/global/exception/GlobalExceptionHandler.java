package com.team23.global.exception;  // 사용자의 현재 구조 따름

import com.team23.global.response.CommonResponse;
import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기.
 *
 * <p>모든 컨트롤러에서 발생한 예외를 표준 ErrorResponse 형식으로 변환한다.
 *
 * <p>처리하는 예외 계층:
 * <ul>
 *   <li>{@link BusinessException} — 비즈니스 규칙 위반 (도메인별 상태 코드)</li>
 *   <li>{@link MethodArgumentNotValidException} — 요청 검증 실패 (400)</li>
 *   <li>{@link IllegalArgumentException} — 잘못된 인자 (400)</li>
 *   <li>{@link Exception} — 예측 못 한 모든 에러 (500)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<?>> handleBusinessException(
            BusinessException e, HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: code={}, path={}",
                errorCode.getCode(), request.getRequestURI());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonResponse.createError(errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request
    ) {
        log.warn("Validation failed: path={}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.createFail(e.getBindingResult()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<?>> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request
    ) {
        log.warn("IllegalArgumentException: path={}, message={}",
                request.getRequestURI(), e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.createError(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleUnexpected(
            Exception e, HttpServletRequest request
    ) {
        log.error("Unexpected exception: path={}", request.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.createError("서버 내부 오류가 발생했습니다"));
    }
}
