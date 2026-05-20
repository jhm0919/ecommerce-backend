package com.team23.management.banner.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record BannerUpdateRequest(
        @NotBlank(message = "배너명은 필수입니다")
        @Size(max = 100, message = "배너명은 100자 이하입니다")
        String name,

        @NotBlank(message = "이미지 URL은 필수입니다")
        @Pattern(
                regexp = "^https?://.+\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "이미지 URL 형식이 올바르지 않습니다"
        )
        String imageUrl,

        @Pattern(
                regexp = "^https?://.+",
                message = "링크 URL 형식이 올바르지 않습니다"
        )
        String linkUrl,

        @NotNull(message = "게시 시작일시는 필수입니다")
        LocalDateTime startAt,

        @NotNull(message = "게시 종료일시는 필수입니다")
        LocalDateTime endAt,

        @NotNull(message = "우선순위는 필수입니다")
        @Min(value = 1, message = "우선순위는 1 이상입니다")
        Integer displayOrder
) {
}
