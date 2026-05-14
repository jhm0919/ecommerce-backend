package com.team23.customer.seller.service;

import com.team23.customer.auth.dto.TokenPair;
import com.team23.customer.auth.service.AuthService;
import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.domain.SellerStatus;
import com.team23.customer.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerAuthService {

    private final SellerRepository sellerRepository;
    private final AuthService authService;           // 재사용
    private final PasswordEncoder passwordEncoder;

    public TokenPair login(String loginId, String rawPassword) {

        // 1. 판매자 조회
        Seller seller = sellerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND) {
                });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, seller.getPassword())) {
            throw new BusinessException(ErrorCode.SELLER_INVALID_PASSWORD) {
            };
        }

        // 3. 정지 계정 검증
        if (seller.getStatus() == SellerStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.SELLER_SUSPENDED) {
            };
        }

        // 4. 토큰 발급 — 팀원 AuthService 재사용
        return authService.issueTokens(
                seller.getId(),
                seller.getLoginId(),  // providerSub 자리에 loginId
                "SELLER"
        );
    }
}
