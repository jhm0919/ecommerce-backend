package com.team23.order.domain;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 주문번호를 생성하는 유틸리티.
 *
 * <p>형식: {@code ORD-YYYYMMDD-XXXXXX}
 * <ul>
 *   <li>ORD: 식별 prefix</li>
 *   <li>YYYYMMDD: 주문일자</li>
 *   <li>XXXXXX: 6자 hex 랜덤 (충돌 방지 + 추측 어려움)</li>
 * </ul>
 *
 * <p>예: {@code ORD-20260428-A3F2B8}
 *
 * <p>랜덤 사용 이유: 순차 ID는 하루 주문 수를 추측 가능하게 함 (비즈니스 정보 누출).
 */
public final class OrderNumberGenerator {

    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RANDOM_BYTES = 3;  // 6자 hex

    private OrderNumberGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 새로운 주문번호를 생성한다.
     */
    public static String generate() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String random = generateRandomHex();
        return PREFIX + "-" + date + "-" + random;
    }

    private static String generateRandomHex() {
        byte[] bytes = new byte[RANDOM_BYTES];
        RANDOM.nextBytes(bytes);

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}