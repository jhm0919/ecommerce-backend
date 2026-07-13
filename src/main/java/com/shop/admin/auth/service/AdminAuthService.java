package com.shop.admin.auth.service;

import com.shop.global.security.auth.dto.TokenPair;
import com.shop.global.security.auth.service.AuthService;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.admin.auth.domain.Admin;
import com.shop.admin.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final AuthService authService;           // 재사용
    private final PasswordEncoder passwordEncoder;

    public TokenPair login(String username, String password) {

        // 1. 판매자 조회
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND) {
                });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(ErrorCode.SELLER_INVALID_PASSWORD) {
            };
        }

        // 4. 토큰 발급 AuthService 재사용
        return authService.createToken(
                admin.getId(),
                admin.getUsername(),  // providerSub 자리에 username
                "SELLER"
        );
    }
}
