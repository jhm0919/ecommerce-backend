package com.team23.customer.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("정상적인 금액으로 생성할 수 있다")
        void createValidMoney() {
            Money money = Money.krw(1000);

            assertThat(money.getAmount()).isEqualByComparingTo("1000");
            assertThat(money.getCurrency()).isEqualTo("KRW");
        }

        @Test
        @DisplayName("0원도 생성 가능하다")
        void createZeroMoney() {
            Money money = Money.krw(0);

            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수 금액은 거부한다")
        void rejectNegativeAmount() {
            assertThatThrownBy(() -> new Money(BigDecimal.valueOf(-100), "KRW"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("amount가 null이면 거부한다")
        void rejectNullAmount() {
            assertThatThrownBy(() -> new Money(null, "KRW"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("currency가 3글자가 아니면 거부한다")
        void rejectInvalidCurrencyCode() {
            assertThatThrownBy(() -> new Money(BigDecimal.TEN, "WON"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new Money(BigDecimal.TEN, "KR"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("덧셈")
    class Addition {

        @Test
        @DisplayName("같은 통화는 더할 수 있다")
        void addSameCurrency() {
            Money a = Money.krw(1000);
            Money b = Money.krw(500);

            assertThat(a.add(b)).isEqualTo(Money.krw(1500));
        }

        @Test
        @DisplayName("0원을 더해도 같은 금액이다")
        void addZero() {
            Money a = Money.krw(1000);

            assertThat(a.add(Money.ZERO_KRW)).isEqualTo(a);
        }

        @Test
        @DisplayName("다른 통화는 더할 수 없다")
        void rejectDifferentCurrency() {
            Money krw = Money.krw(1000);
            Money usd = new Money(BigDecimal.valueOf(10), "USD");

            assertThatThrownBy(() -> krw.add(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }
    }

    @Nested
    @DisplayName("뺄셈")
    class Subtraction {

        @Test
        @DisplayName("큰 금액에서 작은 금액을 뺄 수 있다")
        void subtractValid() {
            Money a = Money.krw(1000);
            Money b = Money.krw(300);

            assertThat(a.subtract(b)).isEqualTo(Money.krw(700));
        }

        @Test
        @DisplayName("결과가 음수가 되면 예외")
        void rejectNegativeResult() {
            Money a = Money.krw(500);
            Money b = Money.krw(1000);

            assertThatThrownBy(() -> a.subtract(b))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("같은 금액을 빼면 0이다")
        void subtractSame() {
            Money a = Money.krw(1000);

            assertThat(a.subtract(a)).isEqualTo(Money.ZERO_KRW);
        }
    }

    @Nested
    @DisplayName("곱셈")
    class Multiplication {

        @Test
        @DisplayName("정수배 곱셈 (수량 × 단가)")
        void multiplyByInteger() {
            Money unitPrice = Money.krw(1500);

            assertThat(unitPrice.multiply(3)).isEqualTo(Money.krw(4500));
        }

        @Test
        @DisplayName("0배 곱하면 0원")
        void multiplyByZero() {
            Money unitPrice = Money.krw(1500);

            assertThat(unitPrice.multiply(0)).isEqualTo(Money.ZERO_KRW);
        }

        @Test
        @DisplayName("음수배 곱셈은 거부")
        void rejectNegativeMultiplier() {
            Money unitPrice = Money.krw(1500);

            assertThatThrownBy(() -> unitPrice.multiply(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("isGreaterThan: 큰 금액이 작은 금액보다 크다")
        void isGreaterThan() {
            assertThat(Money.krw(1000).isGreaterThan(Money.krw(500))).isTrue();
            assertThat(Money.krw(500).isGreaterThan(Money.krw(1000))).isFalse();
            assertThat(Money.krw(1000).isGreaterThan(Money.krw(1000))).isFalse();
        }

        @Test
        @DisplayName("compareTo로 정렬 가능")
        void compareToForSorting() {
            assertThat(Money.krw(1000).compareTo(Money.krw(500))).isPositive();
            assertThat(Money.krw(500).compareTo(Money.krw(1000))).isNegative();
            assertThat(Money.krw(1000).compareTo(Money.krw(1000))).isZero();
        }

        @Test
        @DisplayName("다른 통화 비교는 예외")
        void rejectCompareDifferentCurrency() {
            Money krw = Money.krw(1000);
            Money usd = new Money(BigDecimal.TEN, "USD");

            assertThatThrownBy(() -> krw.isGreaterThan(usd))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("값 동등성")
    class ValueEquality {

        @Test
        @DisplayName("같은 통화의 같은 금액은 동등하다")
        void equalForSameAmountAndCurrency() {
            Money a = Money.krw(1000);
            Money b = Money.krw(1000);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("scale이 달라도 같은 값이면 동등하다")
        void equalDespiteDifferentScale() {
            Money a = new Money(new BigDecimal("1000"), "KRW");
            Money b = new Money(new BigDecimal("1000.00"), "KRW");

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 통화면 다르다")
        void notEqualForDifferentCurrency() {
            Money krw = new Money(BigDecimal.valueOf(1000), "KRW");
            Money usd = new Money(BigDecimal.valueOf(1000), "USD");

            assertThat(krw).isNotEqualTo(usd);
        }
    }
}