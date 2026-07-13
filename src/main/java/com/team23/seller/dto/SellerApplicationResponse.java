package com.team23.seller.dto;

import com.team23.seller.domain.ApplicationStatus;
import com.team23.seller.domain.SellerApplication;

public record SellerApplicationResponse(
        Long applicationId,
        ApplicationStatus status
) {
    public static SellerApplicationResponse from(SellerApplication app) {
        return new SellerApplicationResponse(app.getId(), app.getStatus());
    }
}
