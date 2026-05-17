package com.team23.management.seller.dto;

public record ApproveApplicationResponse(
        Long applicationId,
        Long sellerId,
        String temporaryLoginId,
        String temporaryPassword
) {
}
