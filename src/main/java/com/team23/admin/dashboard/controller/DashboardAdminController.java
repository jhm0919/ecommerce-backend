package com.team23.admin.dashboard.controller;

import com.team23.global.response.CommonResponse;
import com.team23.admin.dashboard.domain.AggregationUnit;
import com.team23.admin.dashboard.dto.PaymentsTimeSeriesResponse;
import com.team23.admin.dashboard.dto.RefundsTimeSeriesResponse;
import com.team23.admin.dashboard.dto.SalesTimeSeriesResponse;
import com.team23.admin.dashboard.service.DashboardAdminService;
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

    @Operation(summary = "결제 시계열 조회",
            description = "기간 내 결제 건수, 총액, 평균 금액을 일별 또는 월별로 집계한다. " +
                    "모든 주문 상태 포함 (CANCELLED 도 결제 시도로 간주). 최대 365일.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "기간 오류 / 필수값 누락 / 잘못된 unit")
    })
    @GetMapping("/payments")
    public ResponseEntity<CommonResponse<PaymentsTimeSeriesResponse>> getPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") AggregationUnit unit
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(dashboardAdminService.getPaymentsTimeSeries(startDate, endDate, unit))
        );
    }

    @Operation(summary = "환불 시계열 조회",
            description = "기간 내 환불 건수와 금액을 일별 또는 월별로 집계한다. " +
                    "현재 도메인은 CANCELLED 주문을 환불로 간주. 최대 365일.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "기간 오류 / 필수값 누락 / 잘못된 unit")
    })
    @GetMapping("/refunds")
    public ResponseEntity<CommonResponse<RefundsTimeSeriesResponse>> getRefunds(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") AggregationUnit unit
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(dashboardAdminService.getRefundsTimeSeries(startDate, endDate, unit))
        );
    }

}
