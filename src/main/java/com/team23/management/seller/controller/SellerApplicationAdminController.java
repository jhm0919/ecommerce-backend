package com.team23.management.seller.controller;

import com.team23.global.response.CommonResponse;
import com.team23.customer.seller.domain.ApplicationStatus;
import com.team23.management.seller.dto.RejectApplicationRequest;
import com.team23.management.seller.service.SellerApplicationAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 - 판매자 입점 신청", description = "어드민의 입점 신청 조회/승인/반려 API")
@RestController
@RequestMapping("/api/admin/seller/applications")
@RequiredArgsConstructor
public class SellerApplicationAdminController {

    private final SellerApplicationAdminService sellerApplicationAdminService;

    @Operation(summary = "입점 신청 목록 조회",
            description = "status 쿼리 파라미터로 PENDING/APPROVED/REJECTED 필터 가능 (생략 시 전체)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<?>> list(
            @RequestParam(required = false) ApplicationStatus status
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        sellerApplicationAdminService.list(status))
        );
    }

    @Operation(summary = "입점 신청 승인",
            description = "PENDING 신청을 승인하고 Seller 계정과 임시 자격증명을 발급한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공 (임시 자격증명 포함)"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "404", description = "신청 없음")
    })
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<CommonResponse<?>> approve(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        "입점 신청이 승인되었습니다",
                        sellerApplicationAdminService.approve(applicationId))
        );
    }

    @Operation(summary = "입점 신청 반려")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "404", description = "신청 없음")
    })
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<CommonResponse<?>> reject(
            @PathVariable Long applicationId,
            @RequestBody(required = false) RejectApplicationRequest request
    ) {
        String reason = (request != null) ? request.reason() : null;
        sellerApplicationAdminService.reject(applicationId, reason);
        return ResponseEntity.ok(
                CommonResponse.createSuccessWithNoContent(
                        "입점 신청이 반려되었습니다"));
    }
}