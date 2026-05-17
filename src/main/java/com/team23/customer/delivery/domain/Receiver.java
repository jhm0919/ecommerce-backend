package com.team23.customer.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 수령인을 표현하는 Value Object.
 *
 * <p>배송, 주문 등에서 "물품을 받을 사람"을 식별한다.
 * Member와는 별개의 개념 — 본인이 아닌 가족/친구 등에게 보낼 수 있다.
 *
 * <p>구성:
 * <ul>
 *   <li>{@code name} — 수령인 이름 (필수)</li>
 *   <li>{@code phone} — 수령인 연락처 (필수)</li>
 * </ul>
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receiver {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_PHONE_LENGTH = 9;
    private static final int MAX_PHONE_LENGTH = 20;

    @Column(name = "receiver_name", nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(name = "receiver_phone", nullable = false, length = MAX_PHONE_LENGTH)
    private String phone;

    /**
     * Receiver 생성.
     *
     * @param name 수령인 이름 (필수, 최대 50자)
     * @param phone 연락처 (필수, 9~20자)
     */
    public Receiver(String name, String phone) {
        validateName(name);
        validatePhone(phone);

        this.name = name.trim();
        this.phone = normalizePhone(phone);
    }

    private void validateName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    private void validatePhone(String phone) {
        Objects.requireNonNull(phone, "phone must not be null");
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        if (digitsOnly.isEmpty()) {
            throw new IllegalArgumentException("phone must contain digits");
        }
        if (digitsOnly.length() < MIN_PHONE_LENGTH) {
            throw new IllegalArgumentException(
                    "phone must have at least " + MIN_PHONE_LENGTH + " digits");
        }
        if (digitsOnly.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException(
                    "phone must not exceed " + MAX_PHONE_LENGTH + " digits");
        }
    }

    /**
     * 전화번호를 정규화한다 (하이픈, 공백 제거).
     * 예: "010-1234-5678" → "01012345678"
     */
    private String normalizePhone(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }

    /**
     * 마스킹된 전화번호 반환 (개인정보 보호 화면용).
     * 예: "01012345678" → "010-****-5678"
     */
    public String getMaskedPhone() {
        if (phone.length() < 4) return phone;

        String last4 = phone.substring(phone.length() - 4);
        String first3 = phone.length() >= 11 ? phone.substring(0, 3) : "***";
        return first3 + "-****-" + last4;
    }

    /**
     * 마스킹된 이름 반환 (개인정보 보호 화면용).
     * 예: "홍길동" → "홍*동", "김철수" → "김*수"
     */
    public String getMaskedName() {
        if (name.length() <= 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";

        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receiver receiver)) return false;
        return Objects.equals(name, receiver.name)
                && Objects.equals(phone, receiver.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone);
    }

    @Override
    public String toString() {
        return "Receiver{name='" + name + "', phone='" + phone + "'}";
    }
}