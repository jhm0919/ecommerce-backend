package com.team23.customer.stock.controller;

import com.team23.customer.stock.dto.ReceiveStockRequest;
import com.team23.customer.stock.dto.ReceiveStockResponse;
import com.team23.customer.stock.dto.StockHistoryResponse;
import com.team23.customer.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/seller/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PatchMapping("/receive")
    public ResponseEntity<ReceiveStockResponse> receive(
            @Valid @RequestBody ReceiveStockRequest request
    ) {
        return ResponseEntity.ok(
                stockService.receive(
                        request.purchaseOrderId(),
                        request.receivedQuantity()
                )
        );
    }

    @GetMapping("/receive-history") // 입고 내역 조회
    public ResponseEntity<Page<StockHistoryResponse>> history(
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                stockService.search(skuId, from, to, pageable)
        );
    }
}
