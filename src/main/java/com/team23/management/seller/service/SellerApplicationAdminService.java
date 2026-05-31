package com.team23.management.seller.service;

import com.team23.common.email.EmailService;
import com.team23.management.seller.email.SellerApprovalEmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;
import com.team23.customer.seller.domain.ApplicationStatus;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.domain.SellerApplication;
import com.team23.customer.seller.repository.SellerApplicationRepository;
import com.team23.customer.seller.repository.SellerRepository;
import com.team23.management.seller.domain.TemporaryCredential;
import com.team23.management.seller.domain.TemporaryCredentialGenerator;
import com.team23.management.seller.dto.ApproveApplicationResponse;
import com.team23.management.seller.dto.SellerApplicationListResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerApplicationAdminService {
    private final SellerApplicationRepository sellerApplicationRepository;
    private final SellerRepository sellerRepository;
    private final TemporaryCredentialGenerator temporaryCredentialGenerator;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ───── 목록 조회 ─────
    @Transactional(readOnly = true)
    public List<SellerApplicationListResponse> list(ApplicationStatus status) {
        List<SellerApplication> apps = (status == null)
                ? sellerApplicationRepository.findAll()
                : sellerApplicationRepository.findAllByStatus(status);

        return apps.stream()
                .map(SellerApplicationListResponse::from)
                .toList();
    }

    // ───── 승인 ─────
    public ApproveApplicationResponse approve(Long applicationId) {
        SellerApplication app = findApplication(applicationId);

        // 1. 신청서 상태 변경 (도메인 캡슐화 — PENDING 검증 포함)
        app.approve();

        // 2. 임시 자격증명 생성
        TemporaryCredential credential = temporaryCredentialGenerator.generate();

        // 3. Seller 계정 생성 (신청서 정보 복사)
        Seller seller = Seller.create(
                credential.loginId(),
                passwordEncoder.encode(credential.rawPassword()),
                app.getId(),
                app.getBusinessName(),
                app.getManagerName(),
                app.getManagerEmail(),
                app.getPhoneNumber(),
                app.getMobileNumber()
        );
        Seller saved = sellerRepository.save(seller);

        // 4. 이메일 발송 (비동기 — 실패해도 승인에 영향 없음)
        String emailContent = SellerApprovalEmailTemplate.build(
                app.getBusinessName(),
                app.getManagerName(),
                credential.loginId(),
                credential.rawPassword()
        );
        emailService.sendHtml(
                app.getManagerEmail(),
                "[이커머스 플랫폼] 입점 신청이 승인되었습니다",
                emailContent
        );

        // 5. 응답 반환 (임시 비밀번호 원문은 응답에도 포함)
        return new ApproveApplicationResponse(
                app.getId(),
                saved.getId(),
                credential.loginId(),
                credential.rawPassword()
        );
    }

    // ───── 반려 ─────
    public void reject(Long applicationId, String reason) {
        SellerApplication app = findApplication(applicationId);
        app.reject();
        // reason 은 일단 받기만 (저장은 별도 이슈)
    }

    // ───── 공통 ─────
    private SellerApplication findApplication(Long applicationId) {
        return sellerApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.APPLICATION_NOT_FOUND) {});
    }
}
