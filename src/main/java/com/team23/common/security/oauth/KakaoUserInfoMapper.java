package com.team23.common.security.oauth;

import com.team23.customer.member.domain.AuthProvider;
import com.team23.customer.member.dto.OAuthUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KakaoUserInfoMapper {

    @SuppressWarnings("unchecked")
    public OAuthUserInfo from(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        // 카카오는 사용자 ID가 Long 타입이므로 String으로 변환
        String providerSub = String.valueOf(attributes.get("id"));

        // kakao_account는 nested map
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        Boolean emailVerified = (Boolean) kakaoAccount.get("is_email_verified");

        // profile도 nested
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");

        return new OAuthUserInfo(
                AuthProvider.KAKAO,
                providerSub,
                email,
                Boolean.TRUE.equals(emailVerified),
                nickname,
                profileImage
        );
    }
}
