package com.team23.admin.settlement.dto;

import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record SettlementConfirmRequest(
        @NotNull(message = "정산 월은 필수입니다")
        YearMonth settledMonth
) {
}
