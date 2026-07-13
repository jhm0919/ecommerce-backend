package com.shop.member.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException(String providerSub) {
        super(ErrorCode.MEMBER_NOT_FOUND, "provierSub=" + providerSub);
    }

    // 추가 — 어드민 조회 시
    public MemberNotFoundException(Long memberId) {
        super(ErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId);
    }
}
