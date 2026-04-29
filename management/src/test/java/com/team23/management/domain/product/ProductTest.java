package com.team23.management.domain.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ProductTest {

    @Test
    @DisplayName("상품 생성 시 초기 상태는 ON_SALE이다")
    void create_initialStatus_isOnSale() {
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
    void create_blankName_throws() {
        assertThatThrownBy(() ->
                Product.create("  ", Category.FASHION, 1000, "d", 1L)
        ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("상품명");
    }

}
