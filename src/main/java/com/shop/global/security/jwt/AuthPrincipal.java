package com.shop.global.security.jwt;

import com.shop.member.domain.MemberRole;

public record AuthPrincipal(
        Long memberId,
        String providerSub,
        MemberRole role
) {
}
