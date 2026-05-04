package com.team23.customer.member.dto;

import com.team23.customer.member.domain.Member;
import com.team23.customer.member.domain.MemberRole;
import com.team23.customer.member.domain.MemberStatus;

import java.time.LocalDateTime;

/**
 * 어드민용 회원 응답.
 * 민감 정보(providerSub)는 제외.
 */
public record MemberAdminResponse(
        Long id,
        String email,
        String name,
        String picture,
        MemberStatus status,
        MemberRole role,
        boolean emailVerified,
        LocalDateTime createdAt
) {
    public static MemberAdminResponse from(Member member) {
        return new MemberAdminResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPicture(),
                member.getStatus(),
                member.getRole(),
                member.isEmailVerified(),
                member.getCreatedAt()
        );
    }
}
