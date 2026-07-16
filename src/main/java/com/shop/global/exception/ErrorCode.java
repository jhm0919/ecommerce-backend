package com.shop.global.exception;

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

    // Category
    CATEGORY_HAS_PRODUCTS(HttpStatus.BAD_REQUEST, "CAT001", "상품이 존재하는 카테고리는 삭제할 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CAT002", "카테고리를 찾을 수 없습니다"),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "CAT003", "이미 존재하는 카테고리입니다"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD001", "주문을 찾을 수 없습니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "ORD002", "재고가 부족합니다"),
    ORDER_ACCESS_DENIED(HttpStatus.NOT_FOUND, "ORD003", "주문을 찾을 수 없습니다"),  // 의도적 NOT_FOUND

    // Cart
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "장바구니 항목을 찾을 수 없습니다"),
    PRODUCT_NOT_PURCHASABLE(HttpStatus.BAD_REQUEST, "C002", "구매할 수 없는 상품입니다"),

    // Auth
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 Refresh Token입니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "CMN001", "잘못된 입력입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN999", "서버 오류가 발생했습니다"),

    // Stock
    RECEIVE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "RH001", "입고 내역을 찾을 수 없습니다"),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "RH002", "이미 취소된 입고 내역입니다"),
    INSUFFICIENT_STOCK_FOR_CANCEL(HttpStatus.BAD_REQUEST, "RH003", "현재 재고가 부족하여 입고 취소가 불가합니다"),

    // OrderAdmin
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "OR001", "처리할 수 없는 주문 상태입니다"),

    // OrderCancel
    ALREADY_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "OR003", "이미 취소된 주문입니다"),

    // Seller login
    ADMIN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AD001", "아이디 또는 비밀번호가 올바르지 않습니다"),
    ADMIN_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AD002", "아이디 또는 비밀번호가 올바르지 않습니다"),

    // Question
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "질문을 찾을 수 없습니다"),
    QUESTION_NOT_DELETABLE(HttpStatus.BAD_REQUEST, "Q002", "답변이 존재하는 질문은 삭제할 수 없습니다"),
    QUESTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Q003", "질문에 접근할 수 없습니다"),
    QUESTION_NOT_OWNER(HttpStatus.FORBIDDEN, "Q004", "본인의 질문만 삭제할 수 있습니다"),

    // Sales
    INVALID_SALES_DATE(HttpStatus.BAD_REQUEST, "DB001", "유효하지 않는 날짜입니다."),
    INVALID_SALES_MONTH(HttpStatus.BAD_REQUEST, "DB002", "유효하지 않는 월입니다.");
//    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "DB003", "조회 기간이 올바르지 않습니다"),
//    DATE_RANGE_TOO_LONG(HttpStatus.BAD_REQUEST, "DB004", "조회 기간은 최대 365일까지 가능합니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}