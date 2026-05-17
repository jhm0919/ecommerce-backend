package com.team23.customer.settlement.controller;

import com.team23.common.response.CommonResponse;
import com.team23.customer.settlement.dto.SettlementConfirmRequest;
import com.team23.customer.settlement.dto.SettlementConfirmResponse;
import com.team23.customer.settlement.dto.SettlementSummaryResponse;
import com.team23.customer.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "정산 관리", description = "판매자 정산 내역 조회 및 확정 API")
@RestController
@RequestMapping("/api/seller/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @Operation(
            summary = "정산 내역 조회",
            description = "CONFIRMED 주문 기준 수수료(3.5%) 공제 후 정산 금액 실시간 계산."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<SettlementSummaryResponse>> search(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(settlementService.search(from, to))
        );
    }

    @Operation(
            summary = "정산 확정",
            description = "대상 월의 CONFIRMED 주문을 정산 확정. 이미 정산된 주문은 제외."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확정 성공"),
            @ApiResponse(responseCode = "400", description = "정산 대상 없음")
    })
    @PatchMapping("/confirm")
    public ResponseEntity<CommonResponse<SettlementConfirmResponse>> confirm(
            @Valid @RequestBody SettlementConfirmRequest request
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(settlementService.confirm(request.settledMonth()))
        );
    }
}
