package com.shop.member.domain;

/**
 * 회원의 활동 상태.
 *
 * <p>상태 의미:
 * <ul>
 *   <li>{@link #PENDING} — 가입 후 추가 정보 입력 대기 (예: 약관 동의, 닉네임 설정)</li>
 *   <li>{@link #ACTIVE} — 정상 활동 중인 회원</li>
 *   <li>{@link #SUSPENDED} — 관리자가 일시 정지 (운영 정책 위반 등)</li>
 *   <li>{@link #WITHDRAWN} — 탈퇴 처리 (논리적 삭제)</li>
 * </ul>
 */
public enum MemberStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    WITHDRAWN;

    /**
     * 로그인 가능한 상태인지 확인.
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * 정지/탈퇴 등 비활성 상태인지 확인.
     */
    public boolean isInactive() {
        return this == SUSPENDED || this == WITHDRAWN;
    }
}