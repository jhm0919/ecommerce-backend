package com.team23.management.banner.controller;

import com.team23.common.response.CommonResponse;
import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.service.BannerAdminService;
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

import java.util.Map;

@Tag(name = "어드민 - 배너 관리", description = "메인 페이지 배너 등록/수정/게시/삭제 API")
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class BannerAdminController {
    private final BannerAdminService bannerAdminService;

    @Operation(summary = "배너 등록",
            description = "신규 배너를 DRAFT 상태로 등록한다. 게시는 별도 PATCH API로 진행.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "필수 항목 누락 / 게시 기간 오류 / 형식 오류"),
            @ApiResponse(responseCode = "409", description = "우선순위 중복")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<?>> create(
            @Valid @RequestBody BannerCreateRequest request
    ) {
        Long bannerId = bannerAdminService.create(request);
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(
                        "배너가 등록되었습니다",
                        Map.of("bannerId", bannerId)
                ));
    }
}
