package com.team23.customer.stats.controller;

import com.team23.common.response.CommonResponse;
import com.team23.customer.stats.dto.SalesStatsResponse;
import com.team23.customer.stats.service.SalesStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "어드민 - 판매 통계", description = "판매 실적 통계 조회 API")
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class SalesStatsController {

    private final SalesStatsService salesStatsService;

    @Operation(
            summary = "판매 실적 통계 조회",
            description = "기간별 총 매출액, 주문/취소 건수, 인기 상품 Top 5 조회. 최대 1년 범위."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "날짜 범위 오류 (startDate > endDate 또는 1년 초과)")
    })
    @GetMapping("/sales")
    public ResponseEntity<CommonResponse<SalesStatsResponse>> getSalesStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(salesStatsService.getSalesStats(startDate, endDate))
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate은 endDate보다 이전이어야 합니다");
        }
        if (startDate.plusDays(365).isBefore(endDate)) {
            throw new IllegalArgumentException("조회 기간은 최대 1년입니다");
        }
    }
}
