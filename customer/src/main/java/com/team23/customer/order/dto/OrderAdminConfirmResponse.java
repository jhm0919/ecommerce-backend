package com.team23.customer.order.dto;

import java.util.List;

public record OrderAdminConfirmResponse(
        int successCount,
        List<Long> confirmedOrderIds
) {
}
