package com.team23.customer.order.controller;

import com.team23.customer.global.response.ApiResponse;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.dto.*;
import com.team23.customer.order.service.OrderAdminService;
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
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderAdminListResponse>>> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.createSuccess(orderAdminService.search(status, from, to, pageable))
        );
    }

    @PatchMapping("/confirm")
    public ResponseEntity<ApiResponse<OrderAdminConfirmResponse>> confirm(
            @Valid @RequestBody OrderAdminConfirmRequest request
            ) {
        return ResponseEntity.ok(
                ApiResponse.createSuccess(orderAdminService.confirm(request.orderIds()))
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderAdminCancelResponse>> cancel(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderAdminCancelRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.createSuccess(
                        orderAdminService.cancel(
                        orderId,
                        request.cancelReason(),
                        request.cancelReasonCode())
                )
        );
    }
}
