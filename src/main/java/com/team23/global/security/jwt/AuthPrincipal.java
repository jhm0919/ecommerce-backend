package com.team23.global.security.jwt;

import com.team23.member.domain.MemberRole;

public record AuthPrincipal(
        Long memberId,
        String providerSub,
        MemberRole role
) {
}
