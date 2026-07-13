package com.shop.qna.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class QuestionNotFoundException extends BusinessException {
    public QuestionNotFoundException(Long questionId) {
        super(ErrorCode.QUESTION_NOT_FOUND, "questionId=" + questionId);
    }
}