package com.team23.customer.stockhistory.controller;

import com.team23.customer.stockhistory.domain.StockChangeType;
import com.team23.customer.stockhistory.dto.StockHistoryResponse;
import com.team23.customer.stockhistory.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
    public ResponseEntity<Page<StockHistoryResponse>> findHistories(
            @PathVariable Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) StockChangeType changeType,
            Pageable pageable
    ) {
        Page<StockHistoryResponse> response = stockHistoryService
                .findHistories(productId, skuId, changeType, pageable)
                .map(StockHistoryResponse::from);

        return ResponseEntity.ok(response);
    }
}