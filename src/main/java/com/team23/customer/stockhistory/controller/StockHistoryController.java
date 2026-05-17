package com.team23.customer.stockhistory.controller;

import com.team23.customer.global.response.CommonResponse;
import com.team23.customer.stockhistory.domain.StockChangeType;
import com.team23.customer.stockhistory.dto.StockHistoryResponse;
import com.team23.customer.stockhistory.service.StockHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 - 재고 이력", description = "재고 변동 이력 조회 API")
@RestController
@RequestMapping("/api/admin/products/{productId}/stock-histories")
@RequiredArgsConstructor
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    /**
     * 재고 변동 이력 조회.
     *
     * GET /api/admin/products/1/stock-histories
     * GET /api/admin/products/1/stock-histories?skuId=100
     * GET /api/admin/products/1/stock-histories?changeType=ORDER
     * GET /api/admin/products/1/stock-histories?skuId=100&changeType=ORDER
     */
    @Operation(summary = "재고 변동 이력 조회",
            description = "상품의 재고 변동 이력. SKU/변동타입 필터 지원.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<Page<StockHistoryResponse>>> findHistories(
            @PathVariable Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) StockChangeType changeType,
            Pageable pageable
    ) {
        Page<StockHistoryResponse> response = stockHistoryService
                .findHistories(productId, skuId, changeType, pageable)
                .map(StockHistoryResponse::from);

        return ResponseEntity.ok(CommonResponse.createSuccess(response));
    }
}