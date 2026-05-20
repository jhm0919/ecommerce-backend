package com.team23.management.banner.domain;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners", indexes = {
        @Index(name = "idx_banner_display_order",
                columnList = "display_order", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "display_order", nullable = false, unique = true)
    private int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static Banner create(
            String name,
            String imageUrl,
            String linkUrl,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int displayOrder
    ) {
        validatePeriod(startAt, endAt);

        Banner banner = new Banner();
        banner.name = name;
        banner.imageUrl = imageUrl;
        banner.linkUrl = linkUrl;
        banner.startAt = startAt;
        banner.endAt = endAt;
        banner.displayOrder = displayOrder;
        banner.status = BannerStatus.DRAFT;
        return banner;
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_BANNER_PERIOD) {};
        }
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.INVALID_BANNER_PERIOD) {};
        }
    }
}
