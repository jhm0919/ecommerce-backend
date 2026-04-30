package com.team23.customer.member.exception;

public class MemberNotFoundException extends BusinessException{
    public MemberNotFoundException(String providerSub) {
        super(ErrorCode.MEMBER_NOT_FOUND, "provierSub=" + providerSub);
    }
}
