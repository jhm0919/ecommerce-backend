package com.team23.admin.seller.domain;

/**
 * 어드민 승인 시 발급되는 임시 자격증명.
 *
 * <p>응답 1회 반환 후엔 다시 조회 불가 (DB엔 BCrypt 해시만 저장).
 */
public record TemporaryCredential(
        String loginId,
        String rawPassword
) {
}
