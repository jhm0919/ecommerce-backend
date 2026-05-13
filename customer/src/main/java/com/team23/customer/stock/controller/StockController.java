package com.team23.customer.stock.controller;

import com.team23.customer.global.response.CommonResponse;
import com.team23.customer.purchaseorder.dto.ReceiveCancelResponse;
import com.team23.customer.stock.dto.*;
import com.team23.customer.stock.service.ReceiveService;
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

    private final ReceiveService receiveService;

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

    @PatchMapping("/receive/{id}/cancel")
    public ResponseEntity<CommonResponse<ReceiveCancelResponse>> cancel(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(CommonResponse.createSuccess(receiveService.cancel(id)));
    }
}
