package com.team23.management.banner.dto;

import com.team23.management.banner.domain.Banner;
import com.team23.management.banner.domain.BannerStatus;

import java.time.LocalDateTime;

public record BannerResponse(
        Long bannerId,
        String name,
        String imageUrl,
        String linkUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int displayOrder,
        BannerStatus status
) {
    public static BannerResponse from(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getName(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.getStartAt(),
                banner.getEndAt(),
                banner.getDisplayOrder(),
                banner.currentStatus()   // ← DB status 가 아닌 동적 계산값
        );
    }
}
