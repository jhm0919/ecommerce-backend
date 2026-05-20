package com.team23.management.banner.controller;

import com.team23.common.response.CommonResponse;
import com.team23.management.banner.domain.BannerStatus;
import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.service.BannerAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "배너 목록 조회",
            description = "displayOrder 오름차순 정렬. status 쿼리 파라미터로 필터 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<?>> list(
            @RequestParam(required = false) BannerStatus status
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(bannerAdminService.list(status))
        );
    }
}
