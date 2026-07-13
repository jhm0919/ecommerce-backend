package com.team23.seller.controller;

import com.team23.global.response.CommonResponse;
import com.team23.seller.dto.SellerApplicationRequest;
import com.team23.seller.dto.SellerApplicationResponse;
import com.team23.seller.service.SellerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "판매자 입점 신청", description = "판매자 플랫폼 입점 신청 API")
@RestController
@RequestMapping("/api/seller/applications")
@RequiredArgsConstructor
public class SellerApplicationController {
    private final SellerApplicationService sellerApplicationService;

    @Operation(
            summary = "입점 신청",
            description = "판매자 입점을 신청한다. 검토 후 담당자 이메일로 안내된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신청 완료"),
            @ApiResponse(responseCode = "400", description = "필수 항목 누락 / 형식 오류"),
            @ApiResponse(responseCode = "409", description = "사업자등록번호 또는 통신판매업신고번호 중복")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<SellerApplicationResponse>> apply(
            @Valid @RequestBody SellerApplicationRequest request
    ) {
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess("입점 신청이 완료되었습니다. 검토 후 담당자 이메일로 안내드립니다.",
                        sellerApplicationService.apply(request)
                ));
    }

}
