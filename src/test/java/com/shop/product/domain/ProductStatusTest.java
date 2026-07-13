package com.shop.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductStatusTest {

    @Test
    @DisplayName("ACTIVE는 사용자에게 노출되고 구매 가능")
    void activeIsVisibleAndPurchasable() {
        assertThat(ProductStatus.ACTIVE.isVisibleToCustomer()).isTrue();
        assertThat(ProductStatus.ACTIVE.isPurchasable()).isTrue();
    }

    @Test
    @DisplayName("SOLD_OUT은 사용자에게 노출되지만 구매 불가")
    void soldOutIsVisibleButNotPurchasable() {
        assertThat(ProductStatus.SOLD_OUT.isVisibleToCustomer()).isTrue();
        assertThat(ProductStatus.SOLD_OUT.isPurchasable()).isFalse();
    }

    @Test
    @DisplayName("DISCONTINUED는 사용자에게 노출되지 않고 구매 불가")
    void discontinuedIsNotVisibleAndNotPurchasable() {
        assertThat(ProductStatus.DISCONTINUED.isVisibleToCustomer()).isFalse();
        assertThat(ProductStatus.DISCONTINUED.isPurchasable()).isFalse();
    }
}