package com.team23.seller.dto;

public record SellerLoginResponse(
        String accessToken,
        boolean isTemporaryPassword
) {
}
