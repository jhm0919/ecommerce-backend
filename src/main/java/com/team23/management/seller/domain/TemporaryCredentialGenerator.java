package com.team23.management.seller.domain;

import com.team23.customer.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 임시 자격증명(loginId + rawPassword) 생성기.
 *
 * <p>loginId: "seller_" + random 6자 (영문 소문자 + 숫자)
 * <p>password: 12자 (영문 대소문자 + 숫자 + 특수문자)
 */
@Component
@RequiredArgsConstructor
public class TemporaryCredentialGenerator {
    private static final String LOGIN_ID_PREFIX = "seller_";
    private static final int LOGIN_ID_RANDOM_LENGTH = 6;
    private static final int PASSWORD_LENGTH = 12;

    private static final String LOGIN_ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*";
    private static final int MAX_LOGIN_ID_RETRY = 10;

    private final SellerRepository sellerRepository;
    private final SecureRandom random = new SecureRandom();

    public TemporaryCredential generate() {
        String loginId = generateUniqueLoginId();
        String rawPassword = generatePassword();
        return new TemporaryCredential(loginId, rawPassword);
    }

    private String generateUniqueLoginId() {
        for (int i = 0; i < MAX_LOGIN_ID_RETRY; i++) {
            String candidate = LOGIN_ID_PREFIX + randomString(LOGIN_ID_CHARS, LOGIN_ID_RANDOM_LENGTH);
            if (!sellerRepository.existsByLoginId(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "임시 loginId 생성 재시도 한계 초과 (" + MAX_LOGIN_ID_RETRY + "회)");
    }

    private String generatePassword() {
        return randomString(PASSWORD_CHARS, PASSWORD_LENGTH);
    }

    private String randomString(String charset, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(random.nextInt(charset.length())));
        }
        return sb.toString();
    }
}
