package com.team23.customer.security.jwt;

import com.team23.customer.member.domain.MemberRole;

public record AuthPrincipal(
        Long memberId,
        String providerSub,
        MemberRole role
) {
}
