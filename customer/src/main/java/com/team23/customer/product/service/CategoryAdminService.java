package com.team23.customer.product.service;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.dto.CategoryCreateRequest;
import com.team23.customer.product.dto.CategoryUpdateRequest;
import com.team23.customer.product.exception.CategoryNotFoundException;
import com.team23.customer.product.exception.DuplicateCategoryException;
import com.team23.customer.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAdminService {

    private final CategoryRepository categoryRepository;

    /**
     * 새 카테고리를 등록한다.
     */
    @Transactional
    public Category create(CategoryCreateRequest request) {
        // 중복 체크
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryException("name", request.name());
        }
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateCategoryException("slug", request.slug());
        }

        Category category = Category.create(request.name(), request.slug());
        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, slug={}", saved.getId(), saved.getSlug());
        return saved;
    }

    /**
     * 카테고리 정보를 수정한다 (PATCH 의미).
     */
    @Transactional
    public Category update(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        if (request.name() != null) {
            category.rename(request.name());
        }
        if (request.slug() != null) {
            category.changeSlug(request.slug());
        }

        log.info("Category updated: id={}", categoryId);
        return category;
    }
}