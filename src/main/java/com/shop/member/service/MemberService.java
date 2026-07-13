package com.shop.member.service;

import com.shop.member.domain.Member;
import com.shop.member.dto.MemberInfo;
import com.shop.member.dto.OAuthUserInfo;
import com.shop.member.exception.MemberNotFoundException;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * OAuth 로그인 유스케이스.
     * 기존 회원이면 프로필 업데이트, 신규면 가입 후 반환한다.
     */
    @Transactional
    public void registerOrLogin(OAuthUserInfo info) {  // ★ 타입 변경
        Member member = memberRepository
                .findByAuthProviderAndProviderSub(info.provider(), info.providerSub())
                .map(existing -> updateProfile(existing, info))
                .orElseGet(() -> register(info)); // 회원 생성
//        return MemberInfo.from(member);
    }

    @Transactional(readOnly = true)
    public MemberInfo findById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("memberId=" + memberId));
        return MemberInfo.from(member);
    }


    private Member register(OAuthUserInfo info) {
        Member newMember = Member.registerFromOAuth(
                info.provider(),
                info.providerSub(),
                info.email(),
                info.emailVerified(),
                info.name(),
                info.picture(),
                null  // locale은 없으면 null
        );
        return memberRepository.save(newMember);
    }

    private Member updateProfile(Member existing, OAuthUserInfo info) {
        existing.updateProfile(info.name(), info.picture(), null);
        if (!existing.getEmail().equals(info.email())) {
            existing.changeEmail(info.email(), info.emailVerified());
        }
        return existing;
    }
}
