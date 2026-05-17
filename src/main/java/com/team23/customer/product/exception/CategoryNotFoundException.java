package com.team23.customer.product.exception;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;

/**
 * 존재하지 않는 카테고리 ID로 조회/참조 시 발생.
 *
 * <p>HTTP 400 (Bad Request)로 응답된다.
 * 사용자가 잘못된 카테고리 ID를 보낸 것이지, 시스템 리소스의 부재가 아님.
 */
public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException(Long categoryId) {
        super(ErrorCode.CATEGORY_NOT_FOUND, "Category not found: id=" + categoryId);
    }
}