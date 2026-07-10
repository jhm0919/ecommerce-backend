package com.team23.customer.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * 금액을 표현하는 Value Object.
 *
 * <p>특징:
 * <ul>
 *   <li>불변(Immutable) — 한 번 생성되면 변경 불가</li>
 *   <li>값 동등성 — 같은 통화의 같은 금액은 동일 객체로 취급</li>
 *   <li>음수 거부 — 가격은 0 이상이어야 함</li>
 *   <li>통화 일치 검증 — 다른 통화끼리 연산 시 예외</li>
 * </ul>
 *
 * <p>JPA에서 {@code @Embeddable}로 다른 엔티티에 임베드된다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 전용
public class Money implements Comparable<Money> {

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

//    @Column(name = "currency", length = 3)
//    private String currency;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
//        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
//        validateCurrencyCode(currency);  // ★ 변경
        this.amount = amount;
    }


//    /**
//     * ISO 4217 표준 통화 코드인지 검증.
//     * Java 표준 라이브러리의 Currency 클래스 활용.
//     */
//    private static void validateCurrencyCode(String code) {
//        try {
//            Currency.getInstance(code);
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid currency code: " + code, e);
//        }
//    }

    /**
     * KRW 단축 생성자.
     * 사용 예: {@code Money.krw(10000)}
     */
    public static Money construct(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    /**
     * 두 금액을 더한다. 통화가 다르면 예외.
     */
    public Money add(Money other) {
//        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount));
    }

    /**
     * 두 금액을 뺀다. 결과가 음수가 되면 예외.
     */
    public Money subtract(Money other) {
//        ensureSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new IllegalStateException(
                    "Cannot subtract: result would be negative ("
                            + this.amount + " - " + other.amount + ")");
        }
        return new Money(result);
    }

    /**
     * 정수배로 곱한다. 수량 × 단가 같은 계산에 사용.
     * 음수 곱셈은 거부 (음수 금액이 발생하므로).
     */
    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative: " + multiplier);
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    /**
     * 다른 금액보다 큰지 비교.
     */
    public boolean isGreaterThan(Money other) {
//        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * 0원인지 확인.
     */
    public boolean isZero() {
        return this.amount.signum() == 0;
    }

//    private void ensureSameCurrency(Money other) {
//        Objects.requireNonNull(other, "other Money must not be null");
//        if (!this.currency.equals(other.currency)) {
//            throw new IllegalArgumentException(
//                    "Currency mismatch: " + this.currency + " vs " + other.currency);
//        }
//    }

    @Override
    public int compareTo(Money other) {
//        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    /**
     * 값 동등성: 같은 통화의 같은 금액이면 같은 객체로 취급.
     * BigDecimal의 equals는 scale도 비교(1.0 ≠ 1.00)하므로 compareTo 사용.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
