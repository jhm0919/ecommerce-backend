package com.team23.customer.stats.controller;

import com.team23.customer.stats.dto.SalesStatsResponse;
import com.team23.customer.stats.service.SalesStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SalesStatsControllerTest {

    @Mock private SalesStatsService salesStatsService;

    @InjectMocks private SalesStatsController salesStatsController;

    private final SalesStatsResponse mockResponse = new SalesStatsResponse(
            new BigDecimal("1250000"), "KRW", 42L, 3L, List.of()
    );

    @Nested
    @DisplayName("날짜 유효성 검증")
    class DateValidation {

        @Test
        @DisplayName("startDate이 endDate보다 늦으면 예외")
        void rejectFromAfterTo() {
            LocalDate startDate = LocalDate.of(2026, 5, 12);
            LocalDate endDate = LocalDate.of(2026, 5, 1);  // startDate > endDate

            assertThatThrownBy(() ->
                    salesStatsController.getSalesStats(startDate, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("startDate");
        }

        @Test
        @DisplayName("1년 초과 기간은 예외")
        void rejectOverOneYear() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 12);  // 1년 이상

            assertThatThrownBy(() ->
                    salesStatsController.getSalesStats(startDate, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1년");
        }

        @Test
        @DisplayName("같은 날짜 (하루 조회) 가능")
        void sameDateAllowed() {
            LocalDate today = LocalDate.of(2026, 5, 12);

            given(salesStatsService.getSalesStats(today, today))
                    .willReturn(mockResponse);

            assertThatCode(() ->
                    salesStatsController.getSalesStats(today, today))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("정상 기간 조회")
        void normalRange() {
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 12);

            given(salesStatsService.getSalesStats(startDate, endDate))
                    .willReturn(mockResponse);

            assertThatCode(() ->
                    salesStatsController.getSalesStats(startDate, endDate))
                    .doesNotThrowAnyException();
        }
    }
}