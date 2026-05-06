package com.team23.customer.purchaseorder.domain;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 발주 번호 생성 유틸리티.
 *
 * 형식: PO-YYYYMMDD-XXXXXX
 * 예: PO-20260505-A3F2B8
 */
public class PurchaseOrderNumberGenerator {

    private static final String PREFIX = "PO";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    private PurchaseOrderNumberGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generate() {
        String date = LocalDate.now().format(DATE_FORMAT);
        return PREFIX + "-" + date + "-" + generateRandomHex();
    }

    private static String generateRandomHex() {
        byte[] bytes = new byte[3];
        RANDOM.nextBytes(bytes);
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
