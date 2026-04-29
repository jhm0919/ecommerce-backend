package com.team23.customer.product.controller;

import com.team23.customer.product.dto.CategoryResponse;
import com.team23.customer.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 모든 카테고리 조회.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        List<CategoryResponse> response = categoryService.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}