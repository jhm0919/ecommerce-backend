package com.team23.customer.order.controller;

import com.team23.common.response.CommonResponse;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.dto.*;
import com.team23.customer.order.service.OrderAdminService;
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

@Tag(name = "주문 관리", description = "판매자 주문 조회 및 처리 API")
@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderAdminService;

    @Operation(summary = "주문 조회", description = "기간 및 상태 필터로 주문 목록 페이지네이션 조회.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<OrderAdminListResponse>>> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(orderAdminService.search(status, from, to, pageable))
        );
    }

    @Operation(
            summary = "주문 확정 (일괄)",
            description = "PENDING 주문을 CONFIRMED 으로 일괄 변경. 하나라도 실패 시 전체 롤백."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확정 성공"),
            @ApiResponse(responseCode = "400", description = "PENDING 아닌 주문 포함"),
            @ApiResponse(responseCode = "404", description = "주문 없음")
    })
    @PatchMapping("/confirm")
    public ResponseEntity<CommonResponse<OrderAdminConfirmResponse>> confirm(
            @Valid @RequestBody OrderAdminConfirmRequest request
            ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(orderAdminService.confirm(request.orderIds()))
        );
    }

    @Operation(
            summary = "주문 강제 취소",
            description = "판매자 귀책으로 주문 강제 취소. 재고 복구 포함."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 취소된 주문"),
            @ApiResponse(responseCode = "404", description = "주문 없음")
    })
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<CommonResponse<OrderAdminCancelResponse>> cancel(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderAdminCancelRequest request
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        orderAdminService.cancel(
                        orderId,
                        request.cancelReason(),
                        request.cancelReasonCode())
                )
        );
    }
}
