package com.team23.management.domain.Stock;

import com.team23.management.domain.Sku.Sku;
import com.team23.management.domain.Sku.SkuOptionInput;
import com.team23.management.domain.Sku.SkuOptionValue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Test
    @DisplayName("Stock 생성 시 quantity가 저장된다")
    void createValidInput() {
        Stock stock = Stock.create(1L, 1);

        assertThat(stock.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("초기 quantity가 음수면 생성 실패")
    void createQuantityMinusThrows() {
        assertThatThrownBy(() ->
                Stock.create(1L, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량");
    }
}