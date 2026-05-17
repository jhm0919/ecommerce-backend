package com.team23.customer.product.controller;

import com.team23.customer.global.response.CommonResponse;
import com.team23.customer.product.domain.Category;
import com.team23.customer.product.dto.CategoryCreateRequest;
import com.team23.customer.product.dto.CategoryResponse;
import com.team23.customer.product.dto.CategoryUpdateRequest;
import com.team23.customer.product.service.CategoryAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 - 카테고리", description = "카테고리 관리 API")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @Operation(summary = "카테고리 등록", description = "새 카테고리를 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        Category category = categoryAdminService.create(request);
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(CategoryResponse.from(category)));
    }

    @Operation(summary = "카테고리 수정", description = "카테고리 정보를 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "카테고리 없음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        Category category = categoryAdminService.update(id, request);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(CategoryResponse.from(category))
        );
    }
}