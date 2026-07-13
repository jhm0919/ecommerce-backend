package com.shop.qna.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class QuestionNotDeletableException extends BusinessException {
    public QuestionNotDeletableException(Long questionId) {
        super(ErrorCode.QUESTION_NOT_DELETABLE,
                "questionId=" + questionId);
    }
}