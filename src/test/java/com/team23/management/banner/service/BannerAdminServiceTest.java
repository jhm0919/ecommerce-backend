package com.team23.management.banner.service;

import com.team23.common.exception.BusinessException;
import com.team23.management.banner.domain.Banner;
import com.team23.management.banner.domain.BannerStatus;
import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.dto.BannerResponse;
import com.team23.management.banner.repository.BannerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BannerAdminServiceTest {
    @Autowired
    BannerAdminService bannerAdminService;
    @Autowired
    BannerRepository bannerRepository;

    @BeforeEach
    void setUp() {
        bannerRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        bannerRepository.deleteAll();
    }

    private BannerCreateRequest createRequest(int displayOrder) {
        return new BannerCreateRequest(
                "여름 세일",
                "https://cdn.example.com/banner1.jpg",
                "https://example.com/sale",
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59),
                displayOrder
        );
    }

    @Test
    @DisplayName("정상 등록 — DRAFT 상태로 저장 + ID 반환")
    void createSuccessSavesDraftBanner() {
        BannerCreateRequest request = createRequest(1);

        Long bannerId = bannerAdminService.create(request);

        assertThat(bannerId).isNotNull();
        Banner saved = bannerRepository.findById(bannerId).orElseThrow();
        assertThat(saved.getName()).isEqualTo("여름 세일");
        assertThat(saved.getStatus()).isEqualTo(BannerStatus.DRAFT);
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("displayOrder 중복 -> 예외")
    void createDuplicateOrderThrowsException() {
        bannerAdminService.create(createRequest(1));

        assertThatThrownBy(() -> bannerAdminService.create(createRequest(1)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("게시 기간 오류 (startAt >= endAt) -> 예외")
    void createInvalidPeriodThrowsException() {
        BannerCreateRequest invalid = new BannerCreateRequest(
                "여름 세일",
                "https://cdn.example.com/banner1.jpg",
                "https://example.com/sale",
                LocalDateTime.of(2026, 6, 30, 23, 59),
                LocalDateTime.of(2026, 6, 1, 0, 0),   // start > end
                1
        );

        assertThatThrownBy(() -> bannerAdminService.create(invalid)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("startAt == endAt → 예외 (기간이 0)")
    void createSameStartEndThrowsException() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 6, 1, 0, 0);
        BannerCreateRequest invalid = new BannerCreateRequest(
                "여름 세일",
                "https://cdn.example.com/banner1.jpg",
                "https://example.com/sale",
                sameTime,
                sameTime,
                1
        );

        assertThatThrownBy(() -> bannerAdminService.create(invalid)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("배너 목록 조회 — displayOrder 오름차순")
    void listAllOrderedByDisplayOrder() {
        bannerAdminService.create(createRequest(2));
        bannerAdminService.create(createRequest(1));
        bannerAdminService.create(createRequest(3));

        List<BannerResponse> result = bannerAdminService.list(null);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(BannerResponse::displayOrder)
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("배너 목록 — 빈 목록 200")
    void listEmptyReturnsEmptyList() {
        List<BannerResponse> result = bannerAdminService.list(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("status=DRAFT 필터 — DRAFT 배너만 반환")
    void listFilterDraftReturnsOnlyDraft() {
        bannerAdminService.create(createRequest(1));
        bannerAdminService.create(createRequest(2));

        List<BannerResponse> result = bannerAdminService.list(BannerStatus.DRAFT);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.status() == BannerStatus.DRAFT);
    }

    @Test
    @DisplayName("status=PUBLISHED 필터 — 게시된 배너 없으면 빈 목록")
    void listFilterPublishedReturnsEmptyWhenNoPublished() {
        bannerAdminService.create(createRequest(1));   // DRAFT 상태

        List<BannerResponse> result = bannerAdminService.list(BannerStatus.PUBLISHED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단건 조회 정상")
    void getOneSuccess() {
        Long bannerId = bannerAdminService.create(createRequest(1));

        BannerResponse response = bannerAdminService.getOne(bannerId);

        assertThat(response.bannerId()).isEqualTo(bannerId);
        assertThat(response.name()).isEqualTo("여름 세일");
        assertThat(response.displayOrder()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(BannerStatus.DRAFT);
    }

    @Test
    @DisplayName("단건 조회 - 존재하지 않는 ID 예외")
    void getOneNotFound_throwsException() {
        assertThatThrownBy(() ->
                bannerAdminService.getOne(999L)
        ).isInstanceOf(BusinessException.class);
    }
}