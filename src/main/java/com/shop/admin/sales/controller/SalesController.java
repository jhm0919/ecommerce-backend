package com.shop.admin.sales.controller;

import com.shop.admin.sales.dto.*;
import com.shop.admin.sales.service.SalesService;
import com.shop.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "어드민 - 매출 통계", description = "매출 통계 조회 API")
@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @Operation(summary = "일별 매출 상세 조회",
            description = "특정 날짜의 상품별 매출 상세 내역을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "필수값 누락 / 유효하지 않는 날짜")
    })
    @GetMapping("/daily")
    public ResponseEntity<CommonResponse<SalesDailyResponse>> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam (defaultValue = "ASC") Sort.Direction sort
            ) {
        return ResponseEntity.ok(CommonResponse.createSuccess(salesService.getDailySales(date, sort)));
    }

    @Operation(summary = "월별 매출 조회",
            description = "선택한 달의 상품별 매출 추이를 조회한다." +
                    "매출이 없는 날은 0원으로 채워서 반환됨")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "필수값 누락 / 유효하지 않는 날짜")
    })
    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<SalesMonthlyResponse>> getMonthlySales(
            @ModelAttribute @Valid SalesMonthlyQueryRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.createSuccess(salesService.getMonthlySales(request.month())));
    }


}
