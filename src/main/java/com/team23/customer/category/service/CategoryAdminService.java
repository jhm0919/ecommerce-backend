package com.team23.customer.category.service;

import com.team23.common.exception.BusinessException;
import com.team23.common.exception.ErrorCode;
import com.team23.customer.category.domain.Category;
import com.team23.customer.category.dto.CategoryCreateRequest;
import com.team23.customer.category.dto.CategoryUpdateRequest;
import com.team23.customer.category.exception.CategoryNotFoundException;
import com.team23.customer.category.exception.DuplicateCategoryException;
import com.team23.customer.category.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
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
    public Category create(CategoryCreateRequest request) {
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
        return savedCategory;
    }

    /**
     * 카테고리 정보를 수정한다 (PATCH 의미).
     */
    @Transactional
    public Category update(Long categoryId, CategoryUpdateRequest request) {
        // 엔티티를 조회
        // 그 엔티티는 @Transactional 안에서 영속 상태가 됨
        // rename() / changeSlug()로 필드 값만 바꿈
        // 메서드 끝날 때 트랜잭션이 커밋됨
        // JPA가 변경된 필드를 감지해서 update SQL을 자동으로 날림
        // 이걸 보통 dirty checking이라고 합니다.
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
        return category;
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