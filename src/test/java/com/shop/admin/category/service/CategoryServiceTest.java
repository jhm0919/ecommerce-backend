package com.shop.admin.category.service;

import com.shop.admin.category.dto.CategoryCreateRequest;
import com.shop.admin.category.dto.CategoryUpdateRequest;
import com.shop.category.domain.Category;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.exception.DuplicateCategoryException;
import com.shop.category.repository.CategoryRepository;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    // ─────────────────────────────────────
    // 카테고리 등록
    // ─────────────────────────────────────

    @Nested
    @DisplayName("카테고리 등록 (create)")
    class Create {

        @Test
        @DisplayName("정상 등록")
        void createNormal() {
            CategoryCreateRequest request =
                    new CategoryCreateRequest("남성 상의", "men-tops");

            given(categoryRepository.existsByName("남성 상의")).willReturn(false);
            given(categoryRepository.existsBySlug("men-tops")).willReturn(false);
            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.create(request);

            assertThat(result.getName()).isEqualTo("남성 상의");
            assertThat(result.getSlug()).isEqualTo("men-tops");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("이름 중복이면 예외")
        void rejectDuplicateName() {
            CategoryCreateRequest request =
                    new CategoryCreateRequest("남성 상의", "men-tops");

            given(categoryRepository.existsByName("남성 상의")).willReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(DuplicateCategoryException.class);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("슬러그 중복이면 예외")
        void rejectDuplicateSlug() {
            CategoryCreateRequest request =
                    new CategoryCreateRequest("새 카테고리", "men-tops");

            given(categoryRepository.existsByName("새 카테고리")).willReturn(false);
            given(categoryRepository.existsBySlug("men-tops")).willReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(DuplicateCategoryException.class);

            verify(categoryRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────
    // 카테고리 수정
    // ─────────────────────────────────────

    @Nested
    @DisplayName("카테고리 수정 (update)")
    class Update {

        @Test
        @DisplayName("이름만 수정")
        void updateNameOnly() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            Category result = categoryService.update(1L,
                    new CategoryUpdateRequest("새 이름", null));

            assertThat(result.getName()).isEqualTo("새 이름");
            assertThat(result.getSlug()).isEqualTo("men-tops");  // 변경 없음
        }

        @Test
        @DisplayName("슬러그만 수정")
        void updateSlugOnly() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            Category result = categoryService.update(1L,
                    new CategoryUpdateRequest(null, "new-slug"));

            assertThat(result.getName()).isEqualTo("남성 상의");  // 변경 없음
            assertThat(result.getSlug()).isEqualTo("new-slug");
        }

        @Test
        @DisplayName("이름과 슬러그 동시 수정")
        void updateNameAndSlug() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            Category result = categoryService.update(1L,
                    new CategoryUpdateRequest("새 이름", "new-slug"));

            assertThat(result.getName()).isEqualTo("새 이름");
            assertThat(result.getSlug()).isEqualTo("new-slug");
        }

        @Test
        @DisplayName("null 필드는 수정 안 함 (PATCH 의미)")
        void updateNullFieldsIgnored() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            Category result = categoryService.update(1L,
                    new CategoryUpdateRequest(null, null));

            assertThat(result.getName()).isEqualTo("남성 상의");
            assertThat(result.getSlug()).isEqualTo("men-tops");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(999L,
                    new CategoryUpdateRequest("이름", null)))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    // ─────────────────────────────────────
    // 카테고리 삭제
    // ─────────────────────────────────────

    @Nested
    @DisplayName("카테고리 삭제 (delete)")
    class Delete {

        @Test
        @DisplayName("정상 삭제")
        void deleteNormal() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.existsByCategoryId(1L)).willReturn(false);

            assertThatCode(() -> categoryService.delete(1L))
                    .doesNotThrowAnyException();

            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(999L))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(categoryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("상품이 존재하는 카테고리는 삭제 불가")
        void rejectCategoryWithProducts() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.existsByCategoryId(1L)).willReturn(true);

            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_HAS_PRODUCTS));

            // ★ 삭제 호출 안 됨 확인
            verify(categoryRepository, never()).delete(any());
        }


        @Test
        @DisplayName("삭제 시 FK 위반이면 CATEGORY_HAS_PRODUCTS로 변환")
        void rejectDeleteWhenIntegrityViolationOnFlush() {Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.existsByCategoryId(1L)).willReturn(false);
            willThrow(new DataIntegrityViolationException("fk"))
                    .given(categoryRepository).flush();

            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_HAS_PRODUCTS));

            verify(categoryRepository).delete(category);
            verify(categoryRepository).flush();
        }
    }
}