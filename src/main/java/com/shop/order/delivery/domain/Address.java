package com.shop.order.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 주소를 표현하는 Value Object.
 *
 * <p>구성:
 * <ul>
 *   <li>{@code zipCode} — 우편번호 (필수)</li>
 *   <li>{@code addressLine1} — 기본 주소 (필수)</li>
 *   <li>{@code addressLine2} — 상세 주소 (선택, null 허용)</li>
 * </ul>
 *
 * <p>여러 도메인(Delivery, MemberAddress 등)에서 재사용된다.
 * Hibernate가 프록시 생성할 수 있도록 protected 기본 생성자를 가진다.
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private static final int MAX_ZIP_CODE_LENGTH = 10;
    private static final int MAX_ADDRESS_LINE_LENGTH = 200;

    @Column(name = "zip_code", nullable = false, length = MAX_ZIP_CODE_LENGTH)
    private String zipCode;

    @Column(name = "address_line1", nullable = false, length = MAX_ADDRESS_LINE_LENGTH)
    private String addressLine1;

    @Column(name = "address_line2", length = MAX_ADDRESS_LINE_LENGTH)
    private String addressLine2;  // null 허용 (상세주소 없을 수 있음)

    /**
     * Address 생성.
     *
     * @param zipCode 우편번호 (null/빈값 불가, 최대 10자)
     * @param addressLine1 기본 주소 (null/빈값 불가, 최대 200자)
     * @param addressLine2 상세 주소 (null 허용, 있으면 최대 200자)
     */
    public Address(String zipCode, String addressLine1, String addressLine2) {
        validateZipCode(zipCode);
        validateAddressLine1(addressLine1);
        validateAddressLine2(addressLine2);

        this.zipCode = zipCode.trim();
        this.addressLine1 = addressLine1.trim();
        this.addressLine2 = (addressLine2 == null) ? null : addressLine2.trim();
    }

    private void validateZipCode(String zipCode) {
        Objects.requireNonNull(zipCode, "zipCode must not be null");
        if (zipCode.isBlank()) {
            throw new IllegalArgumentException("zipCode must not be blank");
        }
        if (zipCode.length() > MAX_ZIP_CODE_LENGTH) {
            throw new IllegalArgumentException(
                    "zipCode must not exceed " + MAX_ZIP_CODE_LENGTH + " characters");
        }
    }

    private void validateAddressLine1(String addressLine1) {
        Objects.requireNonNull(addressLine1, "addressLine1 must not be null");
        if (addressLine1.isBlank()) {
            throw new IllegalArgumentException("addressLine1 must not be blank");
        }
        if (addressLine1.length() > MAX_ADDRESS_LINE_LENGTH) {
            throw new IllegalArgumentException(
                    "addressLine1 must not exceed " + MAX_ADDRESS_LINE_LENGTH + " characters");
        }
    }

    private void validateAddressLine2(String addressLine2) {
        // null은 허용
        if (addressLine2 != null && addressLine2.length() > MAX_ADDRESS_LINE_LENGTH) {
            throw new IllegalArgumentException(
                    "addressLine2 must not exceed " + MAX_ADDRESS_LINE_LENGTH + " characters");
        }
    }

    public String getZipCode() { return zipCode; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }

    /**
     * 상세 주소가 있는지 확인.
     */
    public boolean hasDetailAddress() {
        return addressLine2 != null && !addressLine2.isBlank();
    }

    /**
     * 전체 주소를 한 줄로 표현.
     * 예: "06236 서울시 강남구 테헤란로 152 강남파이낸스센터 10층"
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(zipCode).append(" ").append(addressLine1);
        if (hasDetailAddress()) {
            sb.append(" ").append(addressLine2);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(zipCode, address.zipCode)
                && Objects.equals(addressLine1, address.addressLine1)
                && Objects.equals(addressLine2, address.addressLine2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zipCode, addressLine1, addressLine2);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
