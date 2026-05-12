package com.team23.customer.stats.controller;

import com.team23.customer.stats.dto.SalesStatsResponse;
import com.team23.customer.stats.service.SalesStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class SalesStatsController {

    private final SalesStatsService salesStatsService;

    /**
     * 판매 실적 통계 조회.
     *
     * GET /api/admin/stats/sales?from=2026-05-01&to=2026-05-12
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesStatsResponse> getSalesStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        validateDateRange(from, to);
        return ResponseEntity.ok(salesStatsService.getSalesStats(from, to));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 이전이어야 합니다");
        }
        if (from.plusDays(365).isBefore(to)) {
            throw new IllegalArgumentException("조회 기간은 최대 1년입니다");
        }
    }
}
