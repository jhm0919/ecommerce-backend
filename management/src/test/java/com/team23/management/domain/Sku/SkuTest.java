package com.team23.management.domain.Sku;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

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


}