package com.shop.member.dto;


import com.shop.member.domain.AuthProvider;

import java.util.Objects;

/**
 * OAuth 인증 후 받아온 사용자 정보의 표준화된 표현.
 *
 * <p>각 OAuth provider(Google, Kakao, Naver 등)는 서로 다른 응답 포맷을 가지므로,
 * Provider별 Mapper가 이 객체로 변환한 뒤 Service 계층에 전달한다.
 *
 * <p>여기에는 회원 식별과 프로필 표시에 필수적인 정보만 포함된다.
 */
public record OAuthUserInfo(
        AuthProvider provider,
        String providerSub,
        String email,
        boolean emailVerified,
        String name,
        String picture
) {
    public OAuthUserInfo {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(providerSub, "providerSub must not be null");
        Objects.requireNonNull(email, "email must not be null");
        // name, picture는 nullable (provider에 따라 없을 수 있음)
    }
}
