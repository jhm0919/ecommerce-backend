package com.team23.common.security;

import com.team23.common.security.oauth.GoogleUserInfoMapper;
import com.team23.customer.member.dto.OAuthUserInfo;
import com.team23.customer.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberService memberService;
    private final GoogleUserInfoMapper googleUserInfoMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        OAuthUserInfo userInfo = googleUserInfoMapper.from(oidcUser);

        log.info("OIDC login: provider={}, sub={}",
                userInfo.provider(), userInfo.providerSub());

        memberService.registerOrLogin(userInfo);
        return oidcUser;  // Spring Security가 사용할 표준 객체 반환
    }
}