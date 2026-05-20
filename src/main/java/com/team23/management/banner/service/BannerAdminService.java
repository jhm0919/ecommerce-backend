package com.team23.management.banner.service;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;
import com.team23.management.banner.domain.Banner;
import com.team23.management.banner.domain.BannerStatus;
import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.dto.BannerResponse;
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
}
