package com.team23.customer.settlement.repository;

import com.team23.customer.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    // 이미 정산된 orderId 목록
    Set<Long> findOrderIdsBySettledMonth(String settledMonth);

    // 월별 정산 내역 조회
    List<Settlement> findBySettledMonth(String settledMonth);

    // 중복 확인
    boolean existsByOrderId(Long orderId);
}
