package com.team23.customer.product.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(max = 50) String name,

        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9-]+$")
        String slug
) {
}