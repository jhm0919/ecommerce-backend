package com.shop.member.dto;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String picture,
        LocalDateTime joinedAt
) {
    public static MemberResponse from(MemberInfo info) {
        return new MemberResponse(
                info.id(),
                info.email(),
                info.name(),
                info.picture(),
                info.createdAt()  // 필드명만 joinedAt으로 변경
        );
    }
}
