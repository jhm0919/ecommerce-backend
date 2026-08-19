package com.shop.category.controller;

import com.shop.global.response.CommonResponse;
import com.shop.category.dto.CategoryResponse;
import com.shop.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리", description = "카테고리 조회 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리를 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> list() {
        return ResponseEntity.ok(CommonResponse.createSuccess(categoryService.findAll()));
    }
}