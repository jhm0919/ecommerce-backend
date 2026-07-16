package com.shop.admin.sales.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record SalesMonthlyQueryRequest(
        @NotNull
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth month
) {
}
