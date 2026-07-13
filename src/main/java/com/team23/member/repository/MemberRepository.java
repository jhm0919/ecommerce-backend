package com.team23.member.repository;

import com.team23.member.domain.AuthProvider;
import com.team23.member.domain.Member;
import com.team23.member.domain.MemberStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // ★ 변경: googleSub만이 아니라 (provider, providerSub) 조합으로
    Optional<Member> findByAuthProviderAndProviderSub(
            AuthProvider provider,
            String providerSub
    );

    /**
     * 어드민용 회원 목록 조회.
     * status, keyword 모두 선택적.
     */
    @Query("""
            SELECT m FROM Member m
            WHERE (:status IS NULL OR m.status = :status)
              AND (:keyword IS NULL OR
                   LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Member> findMembersForAdmin(
            @Param("status") MemberStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    boolean existsByAuthProviderAndProviderSub(
            AuthProvider provider,
            String providerSub
    );
}
