package com.shop.member.domain;

public enum MemberRole {
    USER, // 일반 사용자
    ADMIN, // 관리자
    /**
     * Spring Security가 사용하는 형식 (ROLE_ 접두사 포함).
     */
//    public String toSpringSecurityRole() {
//        return "ROLE_" + this.name();
//    }
}
