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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryAdminServiceTest {

    @InjectMocks private CategoryAdminService categoryAdminService;

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;

    // ─────────────────────────────────────
    // 카테고리 등록
    // ─────────────────────────────────────

    @Nested
    @DisplayName("카테고리 등록 (create)")
    class Create {

        @Test
        void 정상_등록() {
            // given
            CategoryAdminCreateRequest request =
                    new CategoryAdminCreateRequest("남성 상의", "men-tops");

            given(categoryRepository.existsByName("남성 상의")).willReturn(false);
            given(categoryRepository.existsBySlug("men-tops")).willReturn(false);
            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CategoryResponse response = categoryAdminService.create(request);

            // then
            assertThat(response.name()).isEqualTo("남성 상의");
            assertThat(response.slug()).isEqualTo("men-tops");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void 이름_중복이면_예외() {
            CategoryAdminCreateRequest request =
                    new CategoryAdminCreateRequest("남성 상의", "men-tops");

            given(categoryRepository.existsByName("남성 상의")).willReturn(true);

            assertThatThrownBy(() -> categoryAdminService.create(request))
                    .isInstanceOf(DuplicateCategoryException.class);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        void 슬러그_중복이면_예외() {
            CategoryAdminCreateRequest request =
                    new CategoryAdminCreateRequest("새 카테고리", "men-tops");

            given(categoryRepository.existsByName("새 카테고리")).willReturn(false);
            given(categoryRepository.existsBySlug("men-tops")).willReturn(true);

            assertThatThrownBy(() -> categoryAdminService.create(request))
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
        void 이름만_수정() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            CategoryResponse response = categoryAdminService.update(1L,
                    new CategoryAdminUpdateRequest("새 이름", null));

            assertThat(response.name()).isEqualTo("새 이름");
            assertThat(response.slug()).isEqualTo("men-tops");  // 변경 없음
        }

        @Test
        void 슬러그만_수정() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            CategoryResponse response = categoryAdminService.update(1L,
                    new CategoryAdminUpdateRequest(null, "new-slug"));

            assertThat(response.name()).isEqualTo("남성 상의");  // 변경 없음
            assertThat(response.slug()).isEqualTo("new-slug");
        }

        @Test
        @DisplayName("이름과 슬러그 동시 수정")
        void updateNameAndSlug() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            CategoryResponse response = categoryAdminService.update(1L,
                    new CategoryAdminUpdateRequest("새 이름", "new-slug"));

            assertThat(response.name()).isEqualTo("새 이름");
            assertThat(response.slug()).isEqualTo("new-slug");
        }

        @Test
        @DisplayName("null 필드는 수정 안 함 (PATCH 의미)")
        void updateNullFieldsIgnored() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

            CategoryResponse response = categoryAdminService.update(1L,
                    new CategoryAdminUpdateRequest(null, null));

            assertThat(response.name()).isEqualTo("남성 상의");
            assertThat(response.slug()).isEqualTo("men-tops");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외")
        void rejectUnknownCategory() {
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryAdminService.update(999L,
                    new CategoryAdminUpdateRequest("이름", null)))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

//    // ─────────────────────────────────────
//    // 카테고리 삭제
//    // ─────────────────────────────────────

    @Nested
    @DisplayName("카테고리 삭제 (delete)")
    class Delete {

        @Test
        @DisplayName("")
        void 정상_삭제() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.existsByCategoryId(1L)).willReturn(false);

            assertThatCode(() -> categoryAdminService.delete(1L))
                    .doesNotThrowAnyException();

            verify(categoryRepository).delete(category);
        }

        @Test
        void 존재하지_않는_카테고리면_예외() {
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryAdminService.delete(999L))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(categoryRepository, never()).delete(any());
        }

        @Test
        void 상품이_존재하는_카테고리는_삭제_불가() {
            Category category = Category.create("남성 상의", "men-tops");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.existsByCategoryId(1L)).willReturn(true);

            assertThatThrownBy(() -> categoryAdminService.delete(1L))
                    .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_HAS_PRODUCTS));

            verify(categoryRepository, never()).delete(any());
        }


        @Test
        void 삭제_시_FK_위반이면_CATEGORY_HAS_PRODUCTS로_변환() {
            Category category = Category.create("남성 상의", "men-tops");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.existsByCategoryId(1L)).willReturn(false);
            willThrow(new DataIntegrityViolationException("fk"))
                    .given(categoryRepository).flush();

            assertThatThrownBy(() -> categoryAdminService.delete(1L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_HAS_PRODUCTS));

            verify(categoryRepository).delete(category);
            verify(categoryRepository).flush();
        }
    }
}