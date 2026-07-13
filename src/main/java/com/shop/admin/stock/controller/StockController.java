package com.shop.admin.stock.controller;

import com.shop.admin.stock.dto.*;
import com.shop.global.response.CommonResponse;
import com.shop.admin.purchaseorder.dto.ReceiveCancelResponse;
import com.shop.admin.stock.service.ReceiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "재고/입고 관리", description = "입고 처리 및 이력 관리 API")
@RestController
@RequestMapping("/api/seller/stocks")
@RequiredArgsConstructor
public class StockController {

    private final ReceiveService receiveService;

    @Operation(summary = "입고 처리", description = "발주 건의 물품 입고 처리. 재고 증가 및 이력 기록.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 처리 성공"),
            @ApiResponse(responseCode = "400", description = "이미 입고된 발주 / 수량 오류"),
            @ApiResponse(responseCode = "404", description = "발주 없음")
    })
    @PatchMapping("/receive")
    public ResponseEntity<CommonResponse<ReceiveStockResponse>> receive(
            @Valid @RequestBody ReceiveStockRequest request
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        receiveService.receive(
                        request.purchaseOrderId(),
                        request.receivedQuantity())
                )
        );
    }

    @Operation(summary = "입고 내역 조회", description = "SKU 및 기간 필터로 입고 이력 조회.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/receive-history") // 입고 내역 조회
    public ResponseEntity<CommonResponse<Page<ReceiveHistoryResponse>>> history(
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(receiveService.search(skuId, from, to, pageable))
        );
    }


    @Operation(summary = "입고 수정", description = "입고 수량 수정. 차이만큼 재고 자동 조정.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "취소된 이력 / 재고 부족"),
            @ApiResponse(responseCode = "404", description = "입고 이력 없음")
    })
    @PatchMapping("/receive/{id}")
    public ResponseEntity<CommonResponse<ReceiveAdjustResponse>> adjust(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveAdjustRequest request
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(receiveService.adjust(
                        id,
                        request.receivedQuantity(),
                        request.reason()
                ))
        );
    }

    @Operation(summary = "입고 취소", description = "입고 이력 취소. 재고 차감 및 발주 REQUESTED 복구.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 취소 / 재고 부족"),
            @ApiResponse(responseCode = "404", description = "입고 이력 없음")
    })
    @PatchMapping("/receive/{id}/cancel")
    public ResponseEntity<CommonResponse<ReceiveCancelResponse>> cancel(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(CommonResponse.createSuccess(receiveService.cancel(id)));
    }
}
