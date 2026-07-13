package com.team23.admin.banner.service;

import com.team23.global.exception.BusinessException;
import com.team23.admin.banner.domain.Banner;
import com.team23.admin.banner.domain.BannerStatus;
import com.team23.admin.banner.dto.BannerCreateRequest;
import com.team23.admin.banner.dto.BannerResponse;
import com.team23.admin.banner.dto.BannerUpdateRequest;
import com.team23.admin.banner.repository.BannerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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

    private BannerUpdateRequest updateRequest(String name, int displayOrder) {
        return new BannerUpdateRequest(
                name,
                "https://cdn.example.com/banner-updated.jpg",
                "https://example.com/updated",
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59),
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

    @Test
    @DisplayName("정상 수정 - 필드 모두 변경됨")
    void updateSuccess_changesAllFields() {
        Long bannerId = bannerAdminService.create(createRequest(1));

        bannerAdminService.update(bannerId, updateRequest("가을 세일", 1));

        Banner updated = bannerRepository.findById(bannerId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("가을 세일");
        assertThat(updated.getImageUrl()).isEqualTo("https://cdn.example.com/banner-updated.jpg");
        assertThat(updated.getStartAt()).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0));
        // status 는 변경되지 않음
        assertThat(updated.getStatus()).isEqualTo(BannerStatus.DRAFT);
    }

    @Test
    @DisplayName("displayOrder 변경 안 함 - 자기 자신은 중복 체크에서 제외")
    void updateSameDisplayOrder_doesNotThrow() {
        Long bannerId = bannerAdminService.create(createRequest(1));

        // 같은 displayOrder=1 로 수정 → 자기 자신이라 중복 아님
        assertThatCode(() ->
                bannerAdminService.update(bannerId, updateRequest("가을 세일", 1))
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 배너의 displayOrder 와 중복 → 예외")
    void updateDuplicateOrder_throwsException() {
        Long bannerId1 = bannerAdminService.create(createRequest(1));
        Long bannerId2 = bannerAdminService.create(createRequest(2));

        // bannerId2 를 displayOrder=1 (bannerId1 과 같음) 으로 수정 시도
        assertThatThrownBy(() ->
                bannerAdminService.update(bannerId2, updateRequest("가을 세일", 1))
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("게시 기간 오류 → 예외")
    void updateInvalidPeriod_throwsException() {
        Long bannerId = bannerAdminService.create(createRequest(1));

        BannerUpdateRequest invalid = new BannerUpdateRequest(
                "가을 세일",
                "https://cdn.example.com/banner-updated.jpg",
                "https://example.com/updated",
                LocalDateTime.of(2026, 7, 31, 23, 59),
                LocalDateTime.of(2026, 7, 1, 0, 0),   // start > end
                1
        );

        assertThatThrownBy(() ->
                bannerAdminService.update(bannerId, invalid)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 bannerId → 예외")
    void updateNotFound_throwsException() {
        assertThatThrownBy(() ->
                bannerAdminService.update(999L, updateRequest("가을 세일", 1))
        ).isInstanceOf(BusinessException.class);
    }

    // ───── publish 자체 동작 ─────

    @Test
    @DisplayName("DRAFT 배너 publish 성공 → DB status PUBLISHED")
    void publishSuccess_changesDbStatusToPublished() {
        Long bannerId = bannerAdminService.create(createRequest(1));

        bannerAdminService.publish(bannerId);

        Banner published = bannerRepository.findById(bannerId).orElseThrow();
        assertThat(published.getStatus()).isEqualTo(BannerStatus.PUBLISHED);
    }

    @Test
    @DisplayName("이미 PUBLISHED 인 배너 publish → 예외")
    void publishAlreadyPublished_throwsException() {
        Long bannerId = bannerAdminService.create(createRequest(1));
        bannerAdminService.publish(bannerId);

        assertThatThrownBy(() ->
                bannerAdminService.publish(bannerId)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 bannerId publish → 예외")
    void publishNotFound_throwsException() {
        assertThatThrownBy(() ->
                bannerAdminService.publish(999L)
        ).isInstanceOf(BusinessException.class);
    }

    // ───── 시간 기반 currentStatus 검증 ─────

    @Test
    @DisplayName("publish 후 startAt 미래 → 응답 SCHEDULED")
    void publishedFutureStartAt_returnsScheduled() {
        // 시작 1년 후, 종료 1년 1달 후
        LocalDateTime futureStart = LocalDateTime.now().plusYears(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusYears(1).plusMonths(1);

        Long bannerId = bannerAdminService.create(new BannerCreateRequest(
                "미래 세일",
                "https://cdn.example.com/banner.jpg",
                "https://example.com/sale",
                futureStart,
                futureEnd,
                1
        ));
        bannerAdminService.publish(bannerId);

        BannerResponse response = bannerAdminService.getOne(bannerId);
        assertThat(response.status()).isEqualTo(BannerStatus.SCHEDULED);
    }

    @Test
    @DisplayName("publish 후 게시 기간 중 → 응답 PUBLISHED")
    void publishedDuringPeriod_returnsPublished() {
        // 시작 1시간 전, 종료 1시간 후 (현재 게시 중)
        LocalDateTime nowMinus = LocalDateTime.now().minusHours(1);
        LocalDateTime nowPlus = LocalDateTime.now().plusHours(1);

        Long bannerId = bannerAdminService.create(new BannerCreateRequest(
                "진행중 세일",
                "https://cdn.example.com/banner.jpg",
                "https://example.com/sale",
                nowMinus,
                nowPlus,
                1
        ));
        bannerAdminService.publish(bannerId);

        BannerResponse response = bannerAdminService.getOne(bannerId);
        assertThat(response.status()).isEqualTo(BannerStatus.PUBLISHED);
    }

    @Test
    @DisplayName("publish 후 endAt 과거 → 응답 EXPIRED")
    void publishedPastEndAt_returnsExpired() {
        // 시작 2일 전, 종료 1일 전 (이미 만료)
        LocalDateTime pastStart = LocalDateTime.now().minusDays(2);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);

        Long bannerId = bannerAdminService.create(new BannerCreateRequest(
                "만료 세일",
                "https://cdn.example.com/banner.jpg",
                "https://example.com/sale",
                pastStart,
                pastEnd,
                1
        ));
        bannerAdminService.publish(bannerId);

        BannerResponse response = bannerAdminService.getOne(bannerId);
        assertThat(response.status()).isEqualTo(BannerStatus.EXPIRED);
    }

    // ───── status 필터링 (시간 기반) ─────

    @Test
    @DisplayName("status=SCHEDULED 필터 - 미래 게시 배너만 반환")
    void listFilterScheduled_returnsOnlyFuture() {
        // 1. SCHEDULED: 미래 시작
        Long bannerScheduledId = bannerAdminService.create(new BannerCreateRequest(
                "예약",
                "https://cdn.example.com/banner1.jpg",
                "https://example.com",
                LocalDateTime.now().plusYears(1),
                LocalDateTime.now().plusYears(2),
                1
        ));
        bannerAdminService.publish(bannerScheduledId);

        // 2. PUBLISHED: 현재 게시 중
        Long bannerPublishedId = bannerAdminService.create(new BannerCreateRequest(
                "현재",
                "https://cdn.example.com/banner2.jpg",
                "https://example.com",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                2
        ));
        bannerAdminService.publish(bannerPublishedId);

        // 3. DRAFT: publish 안 함
        bannerAdminService.create(createRequest(3));

        List<BannerResponse> result = bannerAdminService.list(BannerStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).bannerId()).isEqualTo(bannerScheduledId);
    }

    @Test
    @DisplayName("정상 삭제 - DB 에서 제거됨")
    void deleteSuccess_removesFromDb() {
        Long bannerId = bannerAdminService.create(createRequest(1));

        bannerAdminService.delete(bannerId);

        assertThat(bannerRepository.findById(bannerId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 bannerId 삭제 → 예외")
    void deleteNotFound_throwsException() {
        assertThatThrownBy(() ->
                bannerAdminService.delete(999L)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("PUBLISHED 배너도 삭제 가능 (현재 정책)")
    void deletePublishedBanner_succeeds() {
        Long bannerId = bannerAdminService.create(createRequest(1));
        bannerAdminService.publish(bannerId);

        bannerAdminService.delete(bannerId);

        assertThat(bannerRepository.findById(bannerId)).isEmpty();
    }

    @Test
    @DisplayName("삭제 후 동일한 displayOrder 로 재등록 가능")
    void afterDelete_canReuseDisplayOrder() {
        Long bannerId = bannerAdminService.create(createRequest(1));
        bannerAdminService.delete(bannerId);

        // 같은 displayOrder=1 로 새 배너 등록 → 정상 등록되어야 함
        assertThatCode(() ->
                bannerAdminService.create(createRequest(1))
        ).doesNotThrowAnyException();
    }
}