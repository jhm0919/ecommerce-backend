package com.shop.global.security.auth.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Refresh Token의 SHA-256 해시를 계산한다.
 *
 * <p>토큰 원본은 클라이언트에게만 전달되고, 서버는 해시만 저장한다.
 * 이를 통해 DB가 유출되어도 토큰 원본은 복구할 수 없다.
 */
public final class TokenHasher {

    private static final String ALGORITHM = "SHA-256";

    private TokenHasher() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 토큰의 SHA-256 해시를 64자 hex 문자열로 반환.
     */
    public static String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256은 모든 JVM에서 보장됨 → 발생 불가
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
