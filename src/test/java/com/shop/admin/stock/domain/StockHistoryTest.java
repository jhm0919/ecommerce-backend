package com.shop.admin.stock.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StockHistoryTest {

    @Test
    @DisplayName("주문 이력 생성 — orderId 포함")
    void createOrderHistory() {
        StockHistory history = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockType.ORDER, 3, 50, 47, 1001L
        );

        assertThat(history.getProductId()).isEqualTo(1L);
        assertThat(history.getSkuId()).isEqualTo(100L);
        assertThat(history.getChangeType()).isEqualTo(StockType.ORDER);
        assertThat(history.getQuantity()).isEqualTo(3);
        assertThat(history.getStockBefore()).isEqualTo(50);
        assertThat(history.getStockAfter()).isEqualTo(47);
        assertThat(history.getOrderId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("어드민 이력 생성 — orderId null")
    void createAdminHistory() {
        StockHistory history = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockType.ADMIN_INCREASE, 10, 40, 50, null
        );

        assertThat(history.getChangeType()).isEqualTo(StockType.ADMIN_INCREASE);
        assertThat(history.getOrderId()).isNull();
    }

    @Test
    @DisplayName("재고 복구 이력 — stockAfter가 stockBefore보다 크다")
    void restoreHistory() {
        StockHistory history = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockType.ORDER_CANCEL, 3, 47, 50, 1001L
        );

        assertThat(history.getStockAfter()).isGreaterThan(history.getStockBefore());
    }
}