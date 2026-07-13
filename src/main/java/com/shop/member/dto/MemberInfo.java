package com.shop.member.dto;

import com.shop.member.domain.Member;

import java.time.LocalDateTime;

public record MemberInfo(
        Long id,
        String provider,
        String email,
        boolean emailVerified,
        String name,
        String picture,
        String locale,
        LocalDateTime createdAt
) {
    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getProviderSub(),
                member.getEmail(),
                member.isEmailVerified(),
                member.getName(),
                member.getPicture(),
                member.getLocale(),
                member.getCreatedAt()
        );
    }
}