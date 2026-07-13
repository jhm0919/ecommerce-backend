package com.shop.admin.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 50)
        String name,

        @NotBlank(message = "slug는 필수입니다")
        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "slug는 영문/숫자/하이픈만 가능합니다")
        String slug
) {
}
