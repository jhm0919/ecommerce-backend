package com.team23.customer.seller.service;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.dto.ChangePasswordRequest;
import com.team23.customer.seller.dto.UpdateProfileRequest;
import com.team23.customer.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerService {

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    // ───── 아이디 변경 ─────
    public void changeLoginId(Long sellerId, String newLoginId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND) {});

        seller.changeLoginId(newLoginId);  // 도메인 안에서 동일 ID 검증

        if (sellerRepository.existsByLoginId(newLoginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID) {};
        }
    }

    // ───── 비밀번호 변경 ─────
    public void changePassword(Long sellerId, ChangePasswordRequest request) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND) {});

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), seller.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD) {};
        }

        // 현재 비밀번호와 동일 여부
        if (passwordEncoder.matches(request.newPassword(), seller.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_CURRENT_PASSWORD) {};
        }

        seller.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    // ───── 운영 정보 변경 ─────
    public void updateProfile(Long sellerId, UpdateProfileRequest request) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND) {});

        seller.updateProfile(
                request.businessName(),
                request.managerName(),
                request.managerEmail(),
                request.phoneNumber(),
                request.mobileNumber()
        );
    }

}
