package com.team23.customer.seller.dto;

public record SellerLoginResponse(
        String accessToken,
        boolean isTemporaryPassword
) {
}
