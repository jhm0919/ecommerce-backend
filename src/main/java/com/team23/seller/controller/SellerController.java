package com.team23.seller.controller;

import com.team23.global.response.CommonResponse;
import com.team23.global.security.jwt.AuthPrincipal;
import com.team23.seller.dto.ChangeLoginIdRequest;
import com.team23.seller.dto.ChangePasswordRequest;
import com.team23.seller.dto.UpdateProfileRequest;
import com.team23.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "판매자 정보 수정", description = "판매자 아이디/비밀번호/운영 정보 변경 API")
@RestController
@RequestMapping("/api/seller/me")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @Operation(summary = "아이디 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 아이디와 동일"),
            @ApiResponse(responseCode = "401", description = "미인증"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 아이디")
    })
    @PatchMapping("/login-id")
    public ResponseEntity<CommonResponse<?>> changeLoginId(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ChangeLoginIdRequest request
    ) {
        sellerService.changeLoginId(principal.memberId(), request.newLoginId());
        return ResponseEntity.ok(
                CommonResponse.createSuccessWithNoContent("아이디가 변경되었습니다"));
    }

    @Operation(summary = "비밀번호 변경",
            description = "현재 비밀번호 확인 후 변경. 임시 비밀번호 상태 해제.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 / 동일 비밀번호"),
            @ApiResponse(responseCode = "401", description = "미인증")
    })
    @PatchMapping("/password")
    public ResponseEntity<CommonResponse<?>> changePassword(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        sellerService.changePassword(principal.memberId(), request);
        return ResponseEntity.ok(
                CommonResponse.createSuccessWithNoContent("비밀번호가 변경되었습니다"));
    }

    @Operation(summary = "운영 정보 변경",
            description = "상호명/담당자명/이메일/전화번호 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "필수 항목 누락 / 이메일 형식 오류"),
            @ApiResponse(responseCode = "401", description = "미인증")
    })
    @PatchMapping("/profile")
    public ResponseEntity<CommonResponse<?>> updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        sellerService.updateProfile(principal.memberId(), request);
        return ResponseEntity.ok(
                CommonResponse.createSuccessWithNoContent("운영 정보가 변경되었습니다"));
    }

}
