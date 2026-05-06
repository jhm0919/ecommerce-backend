package com.team23.customer.stock.domain;

import org.assertj.core.api.AssertionsForClassTypes;
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

    @Test
    @DisplayName("정상 수정")
    void adjustSuccess() {
        ReceiveHistory history = ReceiveHistory.of(1L, 1L, 100, 200);

        history.adjust(80, "실제 수령 수량 상이");

        assertThat(history.getAdjustedQuantity()).isEqualTo(80);
        assertThat(history.getAdjustReason()).isEqualTo("실제 수령 수량 상이");
        assertThat(history.getAdjustedAt()).isNotNull();
        assertThat(history.getCurrentQuantity()).isEqualTo(80);
    }

    @Test
    @DisplayName("취소된 이력 수정 → 예외")
    void adjustCancelledThrowsException() {
        ReceiveHistory history = ReceiveHistory.of(1L, 1L, 100, 200);
        history.cancel();

        AssertionsForClassTypes.assertThatThrownBy(() -> history.adjust(80, "사유"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("수량 0 이하 → 예외")
    void adjustZeroQuantityThrowsException() {
        ReceiveHistory history = ReceiveHistory.of(1L, 1L, 100, 200);

        AssertionsForClassTypes.assertThatThrownBy(() -> history.adjust(0, "사유"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("두 번 수정 — getCurrentQuantity 최신값")
    void adjustTwiceCurrentQuantityUpdated() {
        ReceiveHistory history = ReceiveHistory.of(1L, 1L, 100, 200);
        history.adjust(80, "1차 수정");
        history.adjust(90, "2차 수정");

        assertThat(history.getCurrentQuantity()).isEqualTo(90);
    }
}