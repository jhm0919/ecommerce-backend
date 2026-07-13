package com.team23.qna.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class QuestionNotOwnerException extends BusinessException {
    public QuestionNotOwnerException(Long questionId, Long memberId) {
        super(ErrorCode.QUESTION_NOT_OWNER,
                "questionId=" + questionId + ", memberId=" + memberId);
    }
}
