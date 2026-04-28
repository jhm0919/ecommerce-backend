package com.team23.customer.member.repository;

import com.team23.customer.member.domain.AuthProvider;
import com.team23.customer.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // ★ 변경: googleSub만이 아니라 (provider, providerSub) 조합으로
    Optional<Member> findByAuthProviderAndProviderSub(
            AuthProvider provider,
            String providerSub
    );

    boolean existsByAuthProviderAndProviderSub(
            AuthProvider provider,
            String providerSub
    );
}
