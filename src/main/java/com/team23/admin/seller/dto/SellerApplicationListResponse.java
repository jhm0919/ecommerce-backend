package com.team23.admin.seller.dto;

import com.team23.seller.domain.ApplicationStatus;
import com.team23.seller.domain.SellerApplication;

import java.time.LocalDateTime;

public record SellerApplicationListResponse(
        Long applicationId,
        String businessName,
        String managerName,
        String managerEmail,
        ApplicationStatus status,
        LocalDateTime createdAt
) {
    public static SellerApplicationListResponse from(SellerApplication app) {
        return new SellerApplicationListResponse(
                app.getId(),
                app.getBusinessName(),
                app.getManagerName(),
                app.getManagerEmail(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}
