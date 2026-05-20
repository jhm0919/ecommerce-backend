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

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    /**
     * 현재 시각 기준으로 표시할 상태를 계산한다.
     *
     * <p>DB 저장값(DRAFT, PUBLISHED) + 시간 정보로 4가지 응답 상태 결정.
     *
     * <ul>
     *   <li>DB DRAFT → DRAFT</li>
     *   <li>DB PUBLISHED + now < startAt → SCHEDULED</li>
     *   <li>DB PUBLISHED + startAt <= now < endAt → PUBLISHED</li>
     *   <li>DB PUBLISHED + now >= endAt → EXPIRED</li>
     * </ul>
     */
    public BannerStatus currentStatus() {
        if (this.status == BannerStatus.DRAFT) {
            return BannerStatus.DRAFT;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.startAt)) {
            return BannerStatus.SCHEDULED;
        }
        if (!now.isBefore(this.endAt)) {
            return BannerStatus.EXPIRED;
        }
        return BannerStatus.PUBLISHED;
    }

    /**
     * 배너 정보를 일괄 수정한다.
     *
     * <p>게시 기간 유효성을 검증하며, displayOrder 중복 검증은 Service 책임.
     */
    public void update(
            String name,
            String imageUrl,
            String linkUrl,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int displayOrder
    ) {
        validatePeriod(startAt, endAt);

        this.name = name;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.startAt = startAt;
        this.endAt = endAt;
        this.displayOrder = displayOrder;
    }
}
