package com.team23.customer.ai.behavior.repository;

import com.team23.customer.ai.behavior.domain.UserBehaviorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {
    List<UserBehaviorLog> findTop20ByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<UserBehaviorLog> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}
