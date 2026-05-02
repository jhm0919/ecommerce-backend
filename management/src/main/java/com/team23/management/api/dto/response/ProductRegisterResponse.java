package com.team23.management.api.dto.response;

import java.time.LocalDateTime;

public record ProductRegisterResponse(
        Long productId,
        LocalDateTime createdAt
) {}
