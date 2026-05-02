package com.team23.management.domain.sku;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkuTest {

    @Test
    @DisplayName("SKU 생성 시 productId, options, additionalPrice가 저장된다")
    void createValidInput() {
        Sku sku = Sku.create(1L, List.of(
                new SkuOptionInput("색상", "white"),
                new SkuOptionInput("사이즈", "M")),
                0);

        assertThat(sku.getProductId()).isEqualTo(1L);
        assertThat(sku.getOptions()).hasSize(2);
        assertThat(sku.getOptions()).extracting(SkuOptionValue::getName).containsExactly("색상", "사이즈");
        assertThat(sku.getOptions()).extracting(SkuOptionValue::getValue).containsExactly("white", "M");
        assertThat(sku.getAdditionalPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("옵션이 0개면 생성 실패")
    void createOption0Throws() {
        assertThatThrownBy(() ->
                Sku.create(1L,
                        List.of(),
                        0)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("옵션");

    }

    @Test
    @DisplayName("동일 옵션명이 중복되면 실패")
    void createOptionDuplicate() {
        assertThatThrownBy(() ->
                Sku.create(1L,
                        List.of(
                                new SkuOptionInput("색상", "white"),
                                new SkuOptionInput("색상", "white")
                        ),
                        0)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("옵션");
    }

    @Test
    @DisplayName("추가 가격이 음수이면 생성 실패")
    void createPriceMinusThrows() {
        assertThatThrownBy(() ->
                Sku.create(1L,
                        List.of(
                                new SkuOptionInput("색상", "white"),
                                new SkuOptionInput("사이즈", "M")
                        ),
                        -1)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격");
    }

    @Test
    @DisplayName("추가 가격 정상 변경")
    void updateAdditionalPriceValidInput() {
        Sku sku = Sku.create(1L,
                List.of(new SkuOptionInput("색상", "blue")), 1000);

        sku.updateAdditionalPrice(2000);

        assertThat(sku.getAdditionalPrice()).isEqualTo(2000);
    }

    @Test
    @DisplayName("음수 추가 가격 → 예외")
    void updateAdditionalPriceMinusThrows() {
        Sku sku = Sku.create(1L,
                List.of(new SkuOptionInput("색상", "blue")), 1000);

        assertThatThrownBy(() -> sku.updateAdditionalPrice(-100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("추가 가격");
    }
}