package com.team23.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * SKU의 옵션 정보 (이름-값 쌍).
 *
 * <p>예: {@code (색상, 검정)}, {@code (사이즈, S)}.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkuOption {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_VALUE_LENGTH = 50;

    @Column(name = "option_name", nullable = false, length = MAX_NAME_LENGTH)
    private String optionName;

    @Column(name = "option_value", nullable = false, length = MAX_VALUE_LENGTH)
    private String optionValue;

    public SkuOption(String optionName, String optionValue) {
        Objects.requireNonNull(optionName, "optionName must not be null");
        Objects.requireNonNull(optionValue, "optionValue must not be null");

        String trimmedName = optionName.trim();
        String trimmedValue = optionValue.trim();

        validateName(trimmedName);
        validateValue(trimmedValue);

        this.optionName = trimmedName;
        this.optionValue = trimmedValue;
    }

    private static void validateName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("optionName must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "optionName must not exceed " + MAX_NAME_LENGTH + " chars");
        }
    }

    private static void validateValue(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("optionValue must not be blank");
        }
        if (value.length() > MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException(
                    "optionValue must not exceed " + MAX_VALUE_LENGTH + " chars");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkuOption that)) return false;
        return optionName.equals(that.optionName) && optionValue.equals(that.optionValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionName, optionValue);
    }

    @Override
    public String toString() {
        return optionName + "=" + optionValue;
    }
}
