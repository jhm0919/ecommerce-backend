package com.team23.customer.question.exception;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;

public class QuestionNotOwnerException extends BusinessException {
    public QuestionNotOwnerException(Long questionId, Long memberId) {
        super(ErrorCode.QUESTION_NOT_OWNER,
                "questionId=" + questionId + ", memberId=" + memberId);
    }
}
