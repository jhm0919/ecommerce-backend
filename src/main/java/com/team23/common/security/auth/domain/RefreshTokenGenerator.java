package com.team23.common.security.auth.domain;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 안전한 Refresh Token 원본을 생성한다.
 *
 * <p>{@link SecureRandom}을 사용하여 32바이트(256bit) 무작위 값을 생성한 뒤,
 * URL-safe Base64로 인코딩한다. 결과는 약 43자의 문자열이다.
 *
 * <p>JWT가 아닌 무작위 문자열을 사용하는 이유:
 * <ul>
 *   <li>토큰 자체에 정보가 없어 노출되어도 정보 누출 없음</li>
 *   <li>짧고 빠름</li>
 *   <li>추측 불가능 (256bit 엔트로피)</li>
 *   <li>검증은 DB의 해시와 비교만 하면 됨</li>
 * </ul>
 */
public final class RefreshTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;  // 256 bits
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private RefreshTokenGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 새로운 Refresh Token 원본을 생성한다.
     *
     * @return URL-safe Base64로 인코딩된 약 43자 문자열
     */
    public static String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        RANDOM.nextBytes(randomBytes);
        return ENCODER.encodeToString(randomBytes);
    }
}