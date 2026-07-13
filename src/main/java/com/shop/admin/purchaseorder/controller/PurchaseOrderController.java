package com.shop.admin.purchaseorder.controller;

import com.shop.global.response.CommonResponse;
import com.shop.admin.purchaseorder.domain.PurchaseOrder;
import com.shop.admin.purchaseorder.domain.PurchaseOrderStatus;
import com.shop.admin.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.shop.admin.purchaseorder.dto.PurchaseOrderListResponse;
import com.shop.admin.purchaseorder.dto.PurchaseOrderResponse;
import com.shop.admin.purchaseorder.service.PurchaseOrderService;
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

@Tag(name = "발주 관리", description = "재고 보충을 위한 발주 생성 및 조회 API")
@RestController
@RequestMapping("/api/seller/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @Operation(
            summary = "발주 처리",
            description = "SKU 단위로 발주서를 생성한다. DISCONTINUED 상품 발주 불가."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "발주서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "수량 오류 / DISCONTINUED 상품"),
            @ApiResponse(responseCode = "404", description = "SKU 없음")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<PurchaseOrderResponse>> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request
    ) {
        PurchaseOrder po = purchaseOrderService.create(
                request.skuId(),
                request.quantity(),
                request.supplierName(),
                request.supplierContact(),
                request.expectedAt());
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess("발주서 생성 완료",PurchaseOrderResponse.from(po)));
    }

    @Operation(
            summary = "발주 내역 조회",
            description = "기간 및 상태 필터로 발주 목록을 페이지네이션 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<Page<PurchaseOrderListResponse>>> search(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(purchaseOrderService.search(status, from, to, pageable))
        );
    }
}
