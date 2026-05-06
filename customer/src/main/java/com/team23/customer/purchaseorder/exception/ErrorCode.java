package com.team23.customer.purchaseorder.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Purchase Order
    SKU_NOT_FOUND(HttpStatus.NOT_FOUND, "SK001", "SKU를 찾을 수 없습니다"),
    PRODUCT_DISCONTINUED(HttpStatus.BAD_REQUEST, "SK002", "단종된 상품의 SKU에는 발주할 수 없습니다"),
    INVALID_PURCHASE_QUANTITY(HttpStatus.BAD_REQUEST, "PO001", "발주 수량은 1 이상이어야 합니다"),
    PURCHASE_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "PO002", "발주서를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}