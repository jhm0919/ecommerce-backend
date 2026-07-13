package com.team23.qna.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class QuestionNotFoundException extends BusinessException {
    public QuestionNotFoundException(Long questionId) {
        super(ErrorCode.QUESTION_NOT_FOUND, "questionId=" + questionId);
    }
}