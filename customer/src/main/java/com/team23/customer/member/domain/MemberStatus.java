package com.team23.customer.member.domain;

public enum MemberStatus {
    PENDING,    // 추가 정보 입력 대기 (예: 약관 동의)
    ACTIVE,     // 정상 활동
    SUSPENDED,  // 일시 정지 (관리자가 막음)
    WITHDRAWN;  // 탈퇴 (논리적 삭제)
}
