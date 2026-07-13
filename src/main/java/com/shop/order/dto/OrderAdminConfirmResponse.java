package com.shop.order.dto;

import java.util.List;

public record OrderAdminConfirmResponse(
        int successCount,
        List<Long> confirmedOrderIds
) {
}
