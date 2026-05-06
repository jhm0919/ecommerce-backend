package com.team23.customer.purchaseorder.controller;

import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.customer.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.team23.customer.purchaseorder.dto.PurchaseOrderListResponse;
import com.team23.customer.purchaseorder.dto.PurchaseOrderResponse;
import com.team23.customer.purchaseorder.service.PurchaseOrderService;
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
@RequestMapping("/api/seller/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request
    ) {
        PurchaseOrder po = purchaseOrderService.create(
                request.skuId(),
                request.quantity(),
                request.supplierName(),
                request.supplierContact(),
                request.expectedAt());
        return ResponseEntity.status(201).body(PurchaseOrderResponse.from(po));
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderListResponse>> search(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.search(status, from, to, pageable)
        );
    }
}
