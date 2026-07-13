package com.team23.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record MemberProfile(
        String name,
        String givenName,
        String familyName,
        String picture,
        String locale
) {
    public static MemberProfile empty() {
        return new MemberProfile(null, null, null, null, null);
    }
}


