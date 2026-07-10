package com.team23.customer.category.exception;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;

/**
 * 이미 존재하는 이름 또는 slug로 카테고리를 등록하려 할 때 발생.
 *
 * <p>HTTP 409 (Conflict)로 응답된다.
 */
public class DuplicateCategoryException extends BusinessException {

    public DuplicateCategoryException(String field, String value) {
        super(ErrorCode.DUPLICATE_CATEGORY, "Duplicate category: " + field + "=" + value);
    }
}
