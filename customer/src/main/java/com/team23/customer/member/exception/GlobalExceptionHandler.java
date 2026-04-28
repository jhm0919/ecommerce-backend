package com.team23.customer.member.exception;  // 사용자의 현재 구조 따름

import com.team23.customer.member.dto.ErrorResponse;
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

    /**
     * 비즈니스 예외 처리. ErrorCode가 정의한 상태 코드와 메시지를 사용한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("BusinessException: code={}, message={}, path={}, detail={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI(),
                e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI()));
    }

    /**
     * @Valid 검증 실패 처리.
     * 첫 번째 검증 실패 메시지를 응답에 포함한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("요청 검증에 실패했습니다");

        log.warn("Validation failed: path={}, message={}", request.getRequestURI(), message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("V001", message, request.getRequestURI()));
    }

    /**
     * 잘못된 인자 (도메인 검증 등) 처리.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e,
            HttpServletRequest request
    ) {
        log.warn("IllegalArgumentException: path={}, message={}",
                request.getRequestURI(), e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("V002", e.getMessage(), request.getRequestURI()));
    }

    /**
     * 예측하지 못한 모든 예외 처리.
     * 보안상 상세 정보는 응답에 포함하지 않고, 서버 로그에만 기록한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unexpected exception: path={}", request.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "E001",
                        "서버 내부 오류가 발생했습니다",
                        request.getRequestURI()
                ));
    }
}
