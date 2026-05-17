package com.team23.customer.seller.service;

import com.team23.common.exception.BusinessException;
import com.team23.customer.seller.domain.ApplicationStatus;
import com.team23.customer.seller.domain.SellerApplication;
import com.team23.customer.seller.dto.SellerApplicationRequest;
import com.team23.customer.seller.dto.SellerApplicationResponse;
import com.team23.customer.seller.repository.SellerApplicationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SellerApplicationServiceTest {
    @Autowired
    SellerApplicationService sellerApplicationService;
    @Autowired
    SellerApplicationRepository sellerApplicationRepository;

    @BeforeEach
    void setUp() {
        sellerApplicationRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        sellerApplicationRepository.deleteAll();
    }

    // 헬퍼
    private SellerApplicationRequest createRequest(
            String brn, String mosn
    ) {
        return new SellerApplicationRequest(
                "주식회사 예시",
                brn,
                mosn,
                "도매 및 소매업",
                "의류",
                "홍길동",
                "hong@example.com",
                "02-1234-5678",
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("정상 입점 신청 — PENDING 상태 저장")
    void applySuccessSavesPendingApplication() {
        SellerApplicationRequest request =
                createRequest("1234567890", "2024-서울강남-0001");

        SellerApplicationResponse response =
                sellerApplicationService.apply(request);

        assertThat(response.applicationId()).isNotNull();
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(sellerApplicationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("사업자등록번호 중복 → 예외")
    void applyDuplicateBrnThrowsException() {
        sellerApplicationRepository.save(
                SellerApplication.apply(
                        "기존 업체", "1234567890", "2024-서울강남-0001",
                        "도매", "의류", "김철수", "kim@example.com",
                        "02-0000-0000", "010-0000-0000"
                )
        );

        SellerApplicationRequest request = createRequest("1234567890", "2024-서울강남-9999");

        assertThatThrownBy(() ->
                sellerApplicationService.apply(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("통신판매업신고번호 중복 → 예외")
    void applyDuplicateMosnThrowsException() {
        sellerApplicationRepository.save(
                SellerApplication.apply(
                        "기존 업체", "0000000000", "2024-서울강남-0001",
                        "도매", "의류", "김철수", "kim@example.com",
                        "02-0000-0000", "010-0000-0000"
                )
        );

        SellerApplicationRequest request =
                createRequest("1234567890", "2024-서울강남-0001");

        assertThatThrownBy(() ->
                sellerApplicationService.apply(request)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("신청 후 DB 저장 내용 확인")
    void applySuccessSavesCorrectData() {
        sellerApplicationService.apply(
                createRequest("1234567890", "2024-서울강남-0001"));

        SellerApplication saved =
                sellerApplicationRepository.findAll().get(0);

        assertThat(saved.getBusinessName()).isEqualTo("주식회사 예시");
        assertThat(saved.getManagerEmail()).isEqualTo("hong@example.com");
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }


}