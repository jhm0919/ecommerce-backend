package com.shop.admin.member.dto;

import com.shop.member.domain.Member;
import com.shop.member.domain.MemberRole;
import com.shop.member.domain.MemberStatus;

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
                member.getCreatedAt()
        );
    }
}
