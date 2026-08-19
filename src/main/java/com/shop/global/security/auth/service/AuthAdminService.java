package com.shop.global.security.auth.service;

import com.shop.global.security.auth.dto.TokenPair;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.member.domain.Member;
import com.shop.member.domain.MemberRole;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthAdminService {

    private final MemberRepository memberRepository;
    private final AuthService authService;           // 재사용
    private final PasswordEncoder passwordEncoder;

    public TokenPair login(String email, String password) {

        // 판매자 조회
        Member admin = memberRepository.findByEmailAndRole(email, MemberRole.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(ErrorCode.ADMIN_INVALID_PASSWORD) {
            };
        }

        // 토큰 발급 AuthService 재사용
        return authService.createToken(
                admin.getId(),
                admin.getEmail(),  // providerSub 자리에 email
                admin.getRole().name()
        );
    }
}
