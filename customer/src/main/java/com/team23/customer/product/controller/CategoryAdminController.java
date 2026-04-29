package com.team23.customer.product.controller;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.dto.CategoryCreateRequest;
import com.team23.customer.product.dto.CategoryResponse;
import com.team23.customer.product.dto.CategoryUpdateRequest;
import com.team23.customer.product.service.CategoryAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    /**
     * 카테고리 등록.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        Category category = categoryAdminService.create(request);
        return ResponseEntity.status(201)
                .body(CategoryResponse.from(category));
    }

    /**
     * 카테고리 수정.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        Category category = categoryAdminService.update(id, request);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }
}