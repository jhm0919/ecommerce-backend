package com.team23.customer.member.domain;

/**
 * 회원의 권한 역할.
 *
 * <p>역할 의미:
 * <ul>
 *   <li>{@link #USER} — 일반 사용자 (구매자)</li>
 *   <li>{@link #ADMIN} — 시스템 관리자</li>
 * </ul>
 *
 * <p>향후 SELLER, MANAGER 등 확장 가능.
 */
public enum MemberRole {
    USER,
    ADMIN,
    SELLER;   // ★ 추가

    /**
     * Spring Security가 사용하는 형식 (ROLE_ 접두사 포함).
     */
    public String toSpringSecurityRole() {
        return "ROLE_" + this.name();
    }
}
