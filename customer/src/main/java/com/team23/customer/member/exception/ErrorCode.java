package com.team23.customer.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다"),
    MEMBER_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "M002", "활동 중인 회원은 삭제할 수 없습니다"),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "P002", "카테고리를 찾을 수 없습니다"),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "P003", "이미 존재하는 카테고리입니다"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "O002", "재고가 부족합니다"),
    ORDER_ACCESS_DENIED(HttpStatus.NOT_FOUND, "O003", "주문을 찾을 수 없습니다"),  // 의도적 NOT_FOUND

    // Cart
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "장바구니 항목을 찾을 수 없습니다"),
    PRODUCT_NOT_PURCHASABLE(HttpStatus.BAD_REQUEST, "C002", "구매할 수 없는 상품입니다"),

    // Auth
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 Refresh Token입니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다");


    private final HttpStatus status;
    private final String code;
    private final String message;
}