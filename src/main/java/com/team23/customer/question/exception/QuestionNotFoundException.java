package com.team23.customer.question.exception;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;

public class QuestionNotFoundException extends BusinessException {
    public QuestionNotFoundException(Long questionId) {
        super(ErrorCode.QUESTION_NOT_FOUND, "questionId=" + questionId);
    }
}