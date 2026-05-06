package com.team23.customer.stock.controller;

import com.team23.customer.stock.dto.ReceiveStockRequest;
import com.team23.customer.stock.dto.ReceiveStockResponse;
import com.team23.customer.stock.service.ReceiveStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/stocks")
@RequiredArgsConstructor
public class StockController {

    private final ReceiveStockService receiveStockService;

    @PatchMapping("/receive")
    public ResponseEntity<ReceiveStockResponse> receive(
            @Valid @RequestBody ReceiveStockRequest request
    ) {
        return ResponseEntity.ok(
                receiveStockService.receive(
                        request.purchaseOrderId(),
                        request.receivedQuantity()
                )
        );
    }
}
