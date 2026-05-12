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
        @DisplayName("from이 to보다 늦으면 예외")
        void rejectFromAfterTo() {
            LocalDate from = LocalDate.of(2026, 5, 12);
            LocalDate to = LocalDate.of(2026, 5, 1);  // from > to

            assertThatThrownBy(() ->
                    salesStatsController.getSalesStats(from, to))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("from");
        }

        @Test
        @DisplayName("1년 초과 기간은 예외")
        void rejectOverOneYear() {
            LocalDate from = LocalDate.of(2025, 1, 1);
            LocalDate to = LocalDate.of(2026, 5, 12);  // 1년 이상

            assertThatThrownBy(() ->
                    salesStatsController.getSalesStats(from, to))
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
            LocalDate from = LocalDate.of(2026, 5, 1);
            LocalDate to = LocalDate.of(2026, 5, 12);

            given(salesStatsService.getSalesStats(from, to))
                    .willReturn(mockResponse);

            assertThatCode(() ->
                    salesStatsController.getSalesStats(from, to))
                    .doesNotThrowAnyException();
        }
    }
}