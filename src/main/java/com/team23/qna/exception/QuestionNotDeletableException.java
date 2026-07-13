package com.team23.qna.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class QuestionNotDeletableException extends BusinessException {
    public QuestionNotDeletableException(Long questionId) {
        super(ErrorCode.QUESTION_NOT_DELETABLE,
                "questionId=" + questionId);
    }
}