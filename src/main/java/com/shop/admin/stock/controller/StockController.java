package com.shop.admin.stock.controller;

import com.shop.admin.stock.dto.StockUpdateRequest;
import com.shop.global.response.CommonResponse;
import com.shop.admin.stock.domain.StockType;
import com.shop.admin.stock.dto.StockHistoryResponse;
import com.shop.admin.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 - 재고 이력", description = "재고 변동 및 이력 조회 API")
@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "재고 증가", description = "재고를 증가시킨다 (입고).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "증가 성공"),
            @ApiResponse(responseCode = "400", description = "단종 상품"),
            @ApiResponse(responseCode = "404", description = "상품 또는 SKU 없음")
    })
    @PostMapping("/{productId}/skus/{skuId}/stock/increase")
    public ResponseEntity<Void> increaseSkuStock(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        stockService.increaseStock(productId, skuId, request.quantity());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "재고 감소", description = "재고를 수동으로 감소시킨다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "감소 성공"),
            @ApiResponse(responseCode = "400", description = "재고 부족"),
            @ApiResponse(responseCode = "404", description = "상품 또는 SKU 없음")
    })
    @PostMapping("/{productId}/skus/{skuId}/stock/decrease")
    public ResponseEntity<Void> decreaseSkuStock(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        stockService.decreaseStock(productId, skuId, request.quantity());
        return ResponseEntity.noContent().build();
    }

    /**
     * 재고 변동 이력 조회.
     *
     * GET /api/admin/stock/1/
     * GET /api/admin/stock/1/?skuId=100
     * GET /api/admin/stock/1/?changeType=ORDER
     * GET /api/admin/stock/1/?skuId=100&changeType=ORDER
     */
    @Operation(summary = "재고 변동 이력 조회",
            description = "상품의 재고 변동 이력. SKU/변동타입 필터 지원.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponse<Page<StockHistoryResponse>>> findHistories(
            @PathVariable Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) StockType changeType,
            Pageable pageable
    ) {
        Page<StockHistoryResponse> response = stockService
                .findHistories(productId, skuId, changeType, pageable)
                .map(StockHistoryResponse::from);

        return ResponseEntity.ok(CommonResponse.createSuccess(response));
    }
}