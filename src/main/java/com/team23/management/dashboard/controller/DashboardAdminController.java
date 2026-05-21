package com.team23.management.dashboard.controller;

import com.team23.common.response.CommonResponse;
import com.team23.management.dashboard.domain.AggregationUnit;
import com.team23.management.dashboard.dto.SalesTimeSeriesResponse;
import com.team23.management.dashboard.service.DashboardAdminService;
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

@Tag(name = "어드민 - 대시보드", description = "매출 / 결제 / 환불 통계 시계열 조회 API")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardAdminService dashboardAdminService;

    @Operation(summary = "매출 시계열 조회",
            description = "기간(startDate~endDate) 내 매출액과 주문 건수를 일별 또는 월별로 집계한다. " +
                    "CANCELLED 주문은 제외. 최대 365일 조회 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "기간 오류 (역순 / 365일 초과 / 필수값 누락)")
    })
    @GetMapping("/sales")
    public ResponseEntity<CommonResponse<SalesTimeSeriesResponse>> getSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, // 쿼리 파라미터 2026-01-15 같은 ISO 형식 자동 파싱
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") AggregationUnit unit
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(dashboardAdminService.getSalesTimeSeries(startDate, endDate, unit))
        );
    }
}
