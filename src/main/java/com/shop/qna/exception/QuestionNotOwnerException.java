package com.shop.qna.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class QuestionNotOwnerException extends BusinessException {
    public QuestionNotOwnerException(Long questionId, Long memberId) {
        super(ErrorCode.QUESTION_NOT_OWNER,
                "questionId=" + questionId + ", memberId=" + memberId);
    }
}
