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
    void createOvernameThrows() {
        assertThatThrownBy(() ->
                Product.create("a".repeat(101), Category.FASHION, 1000, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("상품명");
    }

    @Test
    @DisplayName("가격이 음수이면 생성 실패")
    void createPriceMinus() {
        assertThatThrownBy(() ->
                Product.create("name", Category.FASHION, -1, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격");
    }

//    "카테고리가 null이면 생성 실패"
//    "가격 변경 시 음수면 실패"
//    "가격 변경 시 정상값이면 변경됨"
//    "이미지 추가 시 images 컬렉션에 들어간다"
//    "DELETED 상태에서 markAsSoldOut 호출 시 실패"
}
