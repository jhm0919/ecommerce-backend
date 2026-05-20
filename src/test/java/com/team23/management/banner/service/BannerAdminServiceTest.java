package com.team23.management.banner.service;

import com.team23.common.exception.BusinessException;
import com.team23.management.banner.domain.Banner;
import com.team23.management.banner.domain.BannerStatus;
import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.repository.BannerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

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
}