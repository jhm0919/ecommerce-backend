package com.team23.common.exception;

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

    // Category
    CATEGORY_HAS_PRODUCTS(HttpStatus.BAD_REQUEST, "P004", "상품이 존재하는 카테고리는 삭제할 수 없습니다"),

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
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다"),

    // Purchase Order
    SKU_NOT_FOUND(HttpStatus.NOT_FOUND, "SK001", "SKU를 찾을 수 없습니다"),
    PRODUCT_DISCONTINUED(HttpStatus.BAD_REQUEST, "SK002", "단종된 상품의 SKU에는 발주할 수 없습니다"),
    INVALID_PURCHASE_QUANTITY(HttpStatus.BAD_REQUEST, "PO001", "발주 수량은 1 이상이어야 합니다"),
    PURCHASE_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "PO002", "발주서를 찾을 수 없습니다"),
    INVALID_PURCHASE_ORDER_STATUS(HttpStatus.BAD_REQUEST, "PO003", "입고 처리할 수 없는 발주 상태입니다"),

    // Receive History
    RECEIVE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "RH001", "입고 내역을 찾을 수 없습니다"),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "RH002", "이미 취소된 입고 내역입니다"),
    INSUFFICIENT_STOCK_FOR_CANCEL(HttpStatus.BAD_REQUEST, "RH003", "현재 재고가 부족하여 입고 취소가 불가합니다"),

    // OrderAdmin
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "OR001", "처리할 수 없는 주문 상태입니다"),

    // OrderCancel
    ALREADY_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "OR003", "이미 취소된 주문입니다"),

    // Settlement confirm
    NO_SETTLEMENT_TARGET(HttpStatus.BAD_REQUEST, "ST001", "정산 확정 대상 주문이 없습니다"),

    // Seller
    DUPLICATE_BUSINESS_REGISTRATION_NUMBER(HttpStatus.CONFLICT, "SA001", "이미 신청된 사업자등록번호입니다"),
    DUPLICATE_MAIL_ORDER_SALES_NUMBER(HttpStatus.CONFLICT, "SA002", "이미 신청된 통신판매업신고번호입니다"),

    // Seller login
    SELLER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "SL001", "아이디 또는 비밀번호가 올바르지 않습니다"),
    SELLER_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "SL002", "아이디 또는 비밀번호가 올바르지 않습니다"),
    SELLER_SUSPENDED(HttpStatus.FORBIDDEN, "SL003", "정지된 판매자 계정입니다"),
    //Seller update
    SAME_AS_CURRENT_LOGIN_ID(HttpStatus.BAD_REQUEST, "SL004", "현재 아이디와 동일합니다"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "SL005", "이미 사용 중인 아이디입니다"),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "SL006", "현재 비밀번호가 올바르지 않습니다"),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "SL007", "현재 비밀번호와 동일합니다"),

    // Seller Application Admin
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SA003", "입점 신청을 찾을 수 없습니다"),
    INVALID_APPLICATION_STATUS(HttpStatus.BAD_REQUEST, "SA004", "처리할 수 없는 신청 상태입니다"),

    // Question
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "질문을 찾을 수 없습니다"),
    QUESTION_NOT_DELETABLE(HttpStatus.BAD_REQUEST, "Q002", "답변이 존재하는 질문은 삭제할 수 없습니다"),
    QUESTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Q003", "질문에 접근할 수 없습니다"),
    QUESTION_NOT_OWNER(HttpStatus.FORBIDDEN, "Q004", "본인의 질문만 삭제할 수 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}