package com.shop.category.controller;

import com.shop.global.response.CommonResponse;
import com.shop.category.domain.Category;
import com.shop.category.dto.CategoryCreateRequest;
import com.shop.category.dto.CategoryResponse;
import com.shop.category.dto.CategoryUpdateRequest;
import com.shop.category.service.CategoryAdminService;
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

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제한다. 상품이 존재하면 삭제 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "상품이 존재하는 카테고리"),
            @ApiResponse(responseCode = "404", description = "카테고리 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}