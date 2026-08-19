package com.shop.admin.category.service;

import com.shop.admin.category.dto.CategoryAdminCreateRequest;
import com.shop.admin.category.dto.CategoryAdminUpdateRequest;
import com.shop.category.domain.Category;
import com.shop.category.dto.CategoryResponse;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.exception.DuplicateCategoryException;
import com.shop.category.repository.CategoryRepository;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAdminService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * 새 카테고리를 등록한다.
     */
    @Transactional
    public CategoryResponse create(CategoryAdminCreateRequest request) {
        // 중복 체크
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryException("name", request.name());
        }
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateCategoryException("slug", request.slug());
        }

        Category category = Category.create(request.name(), request.slug());
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: id={}, slug={}", savedCategory.getId(), savedCategory.getSlug());

        return CategoryResponse.from(savedCategory);
    }

    /**
     * 카테고리 정보를 수정한다 (PATCH 의미).
     */
    @Transactional
    public CategoryResponse update(Long categoryId, CategoryAdminUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // patch이기 때문에 null을 걸러야함
        if (request.name() != null) {
            category.rename(request.name());
        }
        if (request.slug() != null) {
            category.changeSlug(request.slug());
        }

        log.info("Category updated: id={}", categoryId);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // 상품이 존재하면 삭제 불가
        if (productRepository.existsByCategoryId(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_PRODUCTS) {};
        }

        try {
            categoryRepository.delete(category);
            categoryRepository.flush(); // FK 위반을 트랜잭션 내부에서 즉시 감지
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_PRODUCTS) {};
        }
        log.info("Category deleted: id={}, name={}", id, category.getName());
    }
}
