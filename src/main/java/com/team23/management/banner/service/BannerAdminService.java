package com.team23.management.banner.service;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;
import com.team23.management.banner.domain.Banner;
import com.team23.management.banner.domain.BannerStatus;
import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.dto.BannerResponse;
import com.team23.management.banner.dto.BannerUpdateRequest;
import com.team23.management.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BannerAdminService {
    private final BannerRepository bannerRepository;

    public Long create(BannerCreateRequest request) {

        // 1. displayOrder 중복 검증
        if (bannerRepository.existsByDisplayOrder(request.displayOrder())) {
            throw new BusinessException(ErrorCode.DUPLICATE_BANNER_ORDER) {};
        }

        // 2. Banner 생성 (period 검증은 도메인 안에서)
        Banner banner = Banner.create(
                request.name(),
                request.imageUrl(),
                request.linkUrl(),
                request.startAt(),
                request.endAt(),
                request.displayOrder()
        );

        // 3. 저장 + ID 반환
        return bannerRepository.save(banner).getId();
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> list(BannerStatus status) {
        List<Banner> banners = bannerRepository.findAllByOrderByDisplayOrderAsc();

        return banners.stream()
                .map(BannerResponse::from)
                .filter(response -> status == null || response.status() == status)
                .toList();
    }

    @Transactional(readOnly = true)
    public BannerResponse getOne(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND) {});

        return BannerResponse.from(banner);
    }

    public void update(Long bannerId, BannerUpdateRequest request) {

        // 1. 대상 배너 조회
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.BANNER_NOT_FOUND) {});

        // 2. displayOrder 중복 검증 (자기 자신 제외)
        if (bannerRepository.existsByDisplayOrderAndIdNot(
                request.displayOrder(), bannerId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_BANNER_ORDER) {};
        }

        // 3. 도메인 메서드 호출 (period 검증은 도메인 안에서)
        banner.update(
                request.name(),
                request.imageUrl(),
                request.linkUrl(),
                request.startAt(),
                request.endAt(),
                request.displayOrder()
        );

        // 4. save 호출 불필요 — @Transactional + dirty checking 으로 자동 반영
    }

    public void publish(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.BANNER_NOT_FOUND) {});

        banner.publish();   // 도메인이 DRAFT 검증 + 상태 전환

        // dirty checking 으로 UPDATE 자동 반영
    }

    public void delete(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.BANNER_NOT_FOUND) {});

        bannerRepository.delete(banner);
    }
}
