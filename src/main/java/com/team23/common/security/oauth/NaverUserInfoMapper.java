package com.team23.common.security.oauth;

import com.team23.customer.member.domain.AuthProvider;
import com.team23.customer.member.dto.OAuthUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NaverUserInfoMapper {

    @SuppressWarnings("unchecked")
    public OAuthUserInfo from(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return new OAuthUserInfo(
                AuthProvider.NAVER,
                (String) response.get("id"),
                (String) response.get("email"),
                true,  // 네이버는 email_verified 필드 없음, 인증된 이메일만 제공
                (String) response.get("name"),
                (String) response.get("profile_image")
        );
    }
}
