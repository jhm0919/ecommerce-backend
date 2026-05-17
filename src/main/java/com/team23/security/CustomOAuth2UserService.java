package com.team23.security;

import com.team23.customer.member.dto.OAuthUserInfo;
import com.team23.customer.member.service.MemberService;
import com.team23.security.oauth.KakaoUserInfoMapper;
import com.team23.security.oauth.NaverUserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;
    private final KakaoUserInfoMapper kakaoUserInfoMapper;
    private final NaverUserInfoMapper naverUserInfoMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthUserInfo userInfo = switch (registrationId) {
            case "kakao" -> kakaoUserInfoMapper.from(oauth2User);
            case "naver" -> naverUserInfoMapper.from(oauth2User);
            default -> throw new IllegalArgumentException(
                    "Unsupported OAuth2 provider: " + registrationId);
        };

        log.info("OAuth2 login: provider={}, sub={}",
                userInfo.provider(), userInfo.providerSub());

        memberService.registerOrLogin(userInfo);
        return oauth2User;
    }
}