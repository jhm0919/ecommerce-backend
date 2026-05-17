package com.team23.management.seller.service;

import com.team23.global.exception.BusinessException;
import com.team23.customer.seller.domain.ApplicationStatus;
import com.team23.customer.seller.domain.Seller;
import com.team23.customer.seller.domain.SellerApplication;
import com.team23.customer.seller.repository.SellerApplicationRepository;
import com.team23.customer.seller.repository.SellerRepository;
import com.team23.management.seller.dto.ApproveApplicationResponse;
import com.team23.management.seller.dto.SellerApplicationListResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SellerApplicationAdminServiceTest {

    @Autowired SellerApplicationAdminService sellerApplicationAdminService;
    @Autowired SellerApplicationRepository sellerApplicationRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        sellerRepository.deleteAll();
        sellerApplicationRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        sellerRepository.deleteAll();
        sellerApplicationRepository.deleteAll();
    }

    private SellerApplication createApplication(String brn, String mosn) {
        return sellerApplicationRepository.save(
                SellerApplication.apply(
                        "주식회사 예시", brn, mosn,
                        "도매 및 소매업", "의류",
                        "홍길동", "hong@example.com",
                        "02-1234-5678", "010-1234-5678"
                )
        );
    }

    // ───── 목록 ─────
    @Test
    @DisplayName("PENDING 목록 조회")
    void listPendingReturnsOnlyPending() {
        SellerApplication a = createApplication("1111111111", "2024-A-0001");
        SellerApplication b = createApplication("2222222222", "2024-A-0002");
        b.approve();
        sellerApplicationRepository.saveAndFlush(b);

        List<SellerApplicationListResponse> result = sellerApplicationAdminService.list(ApplicationStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicationId()).isEqualTo(a.getId());
    }

    @Test
    @DisplayName("status null - 전체 조회")
    void listNoFilterReturnsAll() {
        createApplication("1111111111", "2024-A-0001");
        createApplication("2222222222", "2024-A-0002");

        List<SellerApplicationListResponse> result = sellerApplicationAdminService.list(null);

        assertThat(result).hasSize(2);
    }

    // ───── 승인 ─────
    @Test
    @DisplayName("승인 성공 - Seller 생성 + 임시 자격증명 응답")
    void approveSuccessCreatesSeller() {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");

        ApproveApplicationResponse response = sellerApplicationAdminService.approve(app.getId());

        // 응답 검증
        assertThat(response.applicationId()).isEqualTo(app.getId());
        assertThat(response.sellerId()).isNotNull();
        assertThat(response.temporaryLoginId()).startsWith("seller_");
        assertThat(response.temporaryPassword()).hasSize(12);

        // 신청서 상태 변경
        SellerApplication updated = sellerApplicationRepository.findById(app.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.APPROVED);

        // Seller 계정 생성 + 임시 비밀번호 해시 저장
        Seller seller = sellerRepository.findById(response.sellerId()).orElseThrow();
        assertThat(seller.getLoginId()).isEqualTo(response.temporaryLoginId());
        assertThat(seller.isTemporaryPassword()).isTrue();
        assertThat(passwordEncoder.matches(response.temporaryPassword(), seller.getPassword())).isTrue();

        // 운영 정보 복사 확인
        assertThat(seller.getBusinessName()).isEqualTo("주식회사 예시");
        assertThat(seller.getManagerEmail()).isEqualTo("hong@example.com");
    }

    @Test
    @DisplayName("이미 APPROVED 인 신청 승인 시도 → 예외")
    void approveAlreadyApprovedThrowsException() {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");
        sellerApplicationAdminService.approve(app.getId());

        assertThatThrownBy(() ->
                sellerApplicationAdminService.approve(app.getId())
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 applicationId → 예외")
    void approveNotFoundThrowsException() {
        assertThatThrownBy(() ->
                sellerApplicationAdminService.approve(999L)
        ).isInstanceOf(BusinessException.class);
    }

    // ───── 반려 ─────
    @Test
    @DisplayName("반려 성공 - 상태 REJECTED")
    void rejectSuccess() {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");

        sellerApplicationAdminService.reject(app.getId(), "사업자 정보 불일치");

        SellerApplication updated = sellerApplicationRepository.findById(app.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        // 반려된 신청은 Seller 생성 X
        assertThat(sellerRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("이미 처리된 신청 반려 → 예외")
    void rejectAlreadyProcessedThrowsException() {
        SellerApplication app = createApplication("1111111111", "2024-A-0001");
        sellerApplicationAdminService.approve(app.getId());

        assertThatThrownBy(() ->
                sellerApplicationAdminService.reject(app.getId(), null)
        ).isInstanceOf(BusinessException.class);
    }
}