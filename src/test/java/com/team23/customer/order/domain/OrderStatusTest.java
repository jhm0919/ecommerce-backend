package com.team23.customer.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("PENDING은 취소 가능")
    void pendingIsCancellable() {
        assertThat(OrderStatus.PENDING.isCancellable()).isTrue();
    }

    @Test
    @DisplayName("CANCELLED는 다시 취소 불가")
    void cancelledIsNotCancellable() {
        assertThat(OrderStatus.CANCELLED.isCancellable()).isFalse();
    }
}