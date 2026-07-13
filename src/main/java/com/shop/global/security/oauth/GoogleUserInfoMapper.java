package com.shop.global.security.oauth;


import com.shop.member.domain.AuthProvider;
import com.shop.member.dto.OAuthUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserInfoMapper {

    public OAuthUserInfo from(OidcUser oidcUser) {
        return new OAuthUserInfo(
                AuthProvider.GOOGLE,
                oidcUser.getSubject(),
                oidcUser.getEmail(),
                Boolean.TRUE.equals(oidcUser.getEmailVerified()),
                oidcUser.getFullName(),
                oidcUser.getPicture()
        );
    }
}
