package com.team23.customer.member.exception;

public class MemberNotFoundException extends BusinessException{
    public MemberNotFoundException(String googleSub) {
        super(ErrorCode.MEMBER_NOT_FOUND, "googleSub=" + googleSub);
    }
}
