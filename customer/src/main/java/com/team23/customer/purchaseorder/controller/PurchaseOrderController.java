package com.team23.customer.purchaseorder.controller;

import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.team23.customer.purchaseorder.dto.PurchaseOrderResponse;
import com.team23.customer.purchaseorder.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request
    ) {
        PurchaseOrder po = purchaseOrderService.create(request);
        return ResponseEntity.status(201).body(PurchaseOrderResponse.from(po));
    }
}
