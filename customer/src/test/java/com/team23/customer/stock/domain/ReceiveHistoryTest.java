package com.team23.customer.stock.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ReceiveHistoryTest {
    @Test
    @DisplayName("정상 취소")
    void cancelSuccess() {
        ReceiveHistory history = ReceiveHistory.of(1L, 1L, 100, 200);
        history.cancel();

        assertThat(history.isCancelled()).isTrue();
        assertThat(history.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 취소된 이력 → 예외")
    void cancelAlreadyCancelledThrowsException() {
        ReceiveHistory history = ReceiveHistory.of(1L, 1L, 100, 200);
        history.cancel();

        assertThatThrownBy(history::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 취소");
    }
}