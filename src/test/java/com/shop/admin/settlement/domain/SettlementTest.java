package com.shop.admin.settlement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementTest {

    @Test
    @DisplayName("Settlement 생성 — CONFIRMED 상태")
    void confirmSettlementHasConfirmedStatus() {
        Settlement s = Settlement.confirm(
                1L,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(1750),
                BigDecimal.valueOf(48250),
                YearMonth.of(2026, 5)
        );

        assertThat(s.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(s.getSettledMonth()).isEqualTo("2026-05");
        assertThat(s.getOrderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("정산금액 = 주문금액 - 수수료")
    void confirmSettlementSettlementAmountIsCorrect() {
        BigDecimal orderAmount = BigDecimal.valueOf(50000);
        BigDecimal fee = BigDecimal.valueOf(1750);
        BigDecimal expected = BigDecimal.valueOf(48250);

        Settlement s = Settlement.confirm(
                1L, orderAmount, fee, expected, YearMonth.of(2026, 5));

        assertThat(s.getSettlementAmount()).isEqualByComparingTo(expected);
    }
}