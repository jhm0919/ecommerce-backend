package com.shop.member.dto;

import com.shop.member.domain.Member;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String picture,
        LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPicture(),
                member.getCreatedAt()
        );
    }
}
