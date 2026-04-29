package com.team23.customer.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다"),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "P002", "카테고리를 찾을 수 없습니다"),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "P003", "이미 존재하는 카테고리입니다"),

    // Auth
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 Refresh Token입니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다");


    private final HttpStatus status;
    private final String code;
    private final String message;
}