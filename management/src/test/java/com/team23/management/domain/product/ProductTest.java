package com.team23.management.domain.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ProductTest {

    @Test
    @DisplayName("상품 생성 시 초기 상태는 ON_SALE이다")
    void createInitialStatusIsOnSale() {
        // given & when
        Product product = Product.create(
                "면 티셔츠",
                Category.FASHION,
                19900,
                "100% 면 소재",
                1L
        );

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);

    }

    @Test
    @DisplayName("상품명이 비어있으면 생성 실패")
    void createBlankNameThrows() {
        assertThatThrownBy(() ->
                Product.create("  ", Category.FASHION, 1000, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("상품명");
    }

    @Test
    @DisplayName("상품명이 100자 초과면 생성 실패")
    void createOverNameThrows() {
        assertThatThrownBy(() ->
                Product.create("a".repeat(101), Category.FASHION, 1000, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("상품명");
    }

    @Test
    @DisplayName("가격이 음수이면 생성 실패")
    void createPriceMinusThrows() {
        assertThatThrownBy(() ->
                Product.create("name", Category.FASHION, -1, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격");
    }

    @Test
    @DisplayName("카테고리가 null이면 생성 실패")
    void createCategoryNullThrows() {
        assertThatThrownBy(() ->
                Product.create("name", null, 1000, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("카테고리");
    }

    @Test
    @DisplayName("상품명 수정")
    void updateNameValidInputChangesName() {
        //given
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);

        //when
        product.updateName("새 이름");

        //then
        assertThat(product.getName()).isEqualTo("새 이름");
    }

    @Test
    @DisplayName("상품명 빈칸일 때 검증")
    void updateNameBlankThrows() {
        //given
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);

        //when
        //then
        assertThatThrownBy(() -> product.updateName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품명");
    }

    @Test
    @DisplayName("상품명 길이가 100자 넘을 때 검증")
    void updateNameOverLengthThrows() {
        //given
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);

        //when
        //then
        assertThatThrownBy(() -> product.updateName("a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품명");
    }

    @Test
    @DisplayName("상품 상태가 삭제상태일 때 수정 가능여부")
    void updateStatusDeletedThrows() {
        //given
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);

        //when
        product.delete();

        //then
        assertThatThrownBy(() -> product.updateName("새 이름"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된");
    }

    @Test
    @DisplayName("가격을 음수로 수정했을 때")
    void updatePriceMinusThrows() {
        //given
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);

        //when
        //then
        assertThatThrownBy(() -> product.updatePrice(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격");
    }

    @Test
    @DisplayName("카테고리를 넣지 않았을 때")
    void updateCategoryNullThrows() {
        //given
        Product product = Product.create("원래 이름", Category.FASHION, 10000, "d", 1L);

        //when
        //then
        assertThatThrownBy(() -> product.updateCategory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("카테고리");
    }
}
