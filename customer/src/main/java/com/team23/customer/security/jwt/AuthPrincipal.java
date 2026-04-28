package com.team23.customer.security.jwt;

public record AuthPrincipal(
        Long memberId,
        String providerSub
) {
}
