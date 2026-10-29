package com.shop.member.service;

import com.shop.member.domain.Member;
import com.shop.member.dto.MemberRegisterRequest;
import com.shop.member.dto.MemberResponse;
import com.shop.member.exception.MemberNotFoundException;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse register(MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.register(request.email(), encodedPassword, request.name(), request.picture());

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("memberId=" + memberId));
        return MemberResponse.from(member);
    }


}
