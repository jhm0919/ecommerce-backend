package com.team23.customer.seller.dto;

import com.team23.customer.seller.domain.ApplicationStatus;
import com.team23.customer.seller.domain.SellerApplication;

public record SellerApplicationResponse(
        Long applicationId,
        ApplicationStatus status
) {
    public static SellerApplicationResponse from(SellerApplication app) {
        return new SellerApplicationResponse(app.getId(), app.getStatus());
    }
}
