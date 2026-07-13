package com.team23.global.security.auth.repository;

import com.team23.global.security.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 해시로 RefreshToken 조회.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * 특정 회원의 모든 활성 토큰을 일괄 무효화.
     * 사용 사례: 비밀번호 변경, 강제 로그아웃 등.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now " +
            "WHERE rt.memberId = :memberId AND rt.revokedAt IS NULL")
    int revokeAllByMemberId(@Param("memberId") Long memberId,
                            @Param("now") LocalDateTime now);

    /**
     * 만료된 토큰 일괄 삭제 (정기 정리용 배치).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}
