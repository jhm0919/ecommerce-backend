package com.team23.customer.member.dto;  // 사용자의 현재 구조 따름

import com.team23.common.exception.ErrorCode;

import java.time.LocalDateTime;

/**
 * API 에러 응답의 표준 형식.
 *
 * <p>모든 에러 응답은 이 형식을 따른다.
 * 클라이언트는 {@code code}로 에러 종류를 구분하고,
 * {@code timestamp}와 {@code path}로 서버 로그와 매칭할 수 있다.
 */
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        String path
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                path
        );
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, LocalDateTime.now(), path);
    }
}