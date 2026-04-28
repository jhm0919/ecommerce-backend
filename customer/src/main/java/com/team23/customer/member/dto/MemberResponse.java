package com.team23.customer.member.dto;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        boolean emailVerified,
        String name,
        String picture,
        String locale,
        LocalDateTime joinedAt
) {
    public static MemberResponse from(MemberInfo info) {
        return new MemberResponse(
                info.id(),
                info.email(),
                info.emailVerified(),
                info.name(),
                info.picture(),
                info.locale(),
                info.createdAt()  // 필드명만 joinedAt으로 변경
        );
    }
}
