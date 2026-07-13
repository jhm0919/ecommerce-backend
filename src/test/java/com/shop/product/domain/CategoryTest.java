package com.shop.product.domain;

import com.shop.category.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CategoryTest {

    @Nested
    @DisplayName("생성 (create)")
    class Create {

        @Test
        @DisplayName("정상적인 카테고리를 생성할 수 있다")
        void createValid() {
            Category category = Category.create("남성 상의", "men-tops");

            assertThat(category.getName()).isEqualTo("남성 상의");
            assertThat(category.getSlug()).isEqualTo("men-tops");
        }

        @Test
        @DisplayName("name과 slug는 trim된다")
        void trimWhitespace() {
            Category category = Category.create("  남성 상의  ", "  men-tops  ");

            assertThat(category.getName()).isEqualTo("남성 상의");
            assertThat(category.getSlug()).isEqualTo("men-tops");
        }

        @Test
        @DisplayName("slug는 항상 소문자로 정규화된다")
        void normalizeSlugToLowerCase() {
            Category category = Category.create("Men Tops", "MEN-TOPS");

            assertThat(category.getSlug()).isEqualTo("men-tops");
        }

        @Test
        @DisplayName("name이 null/blank면 예외")
        void rejectInvalidName() {
            assertThatThrownBy(() -> Category.create(null, "men-tops"))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> Category.create("", "men-tops"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Category.create("   ", "men-tops"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("slug에 한글이나 공백이 들어가면 예외")
        void rejectInvalidSlug() {
            assertThatThrownBy(() -> Category.create("이름", "남성-상의"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Category.create("이름", "men tops"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Category.create("이름", "men_tops"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("수정")
    class Update {

        @Test
        @DisplayName("이름을 변경할 수 있다")
        void rename() {
            Category category = Category.create("남성 상의", "men-tops");

            category.rename("남성 티셔츠");

            assertThat(category.getName()).isEqualTo("남성 티셔츠");
        }

        @Test
        @DisplayName("slug를 변경할 수 있다")
        void changeSlug() {
            Category category = Category.create("남성 상의", "men-tops");

            category.changeSlug("MEN-T-SHIRTS");

            assertThat(category.getSlug()).isEqualTo("men-t-shirts");  // 소문자 정규화
        }
    }
}