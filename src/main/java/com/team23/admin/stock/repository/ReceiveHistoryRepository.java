package com.team23.admin.stock.repository;

import com.team23.admin.stock.domain.ReceiveHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReceiveHistoryRepository extends JpaRepository<ReceiveHistory, Long> {
    Page<ReceiveHistory> findBySkuIdOrderByCreatedAtDesc(Long skuId, Pageable pageable);

    Page<ReceiveHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT sh FROM ReceiveHistory sh
        WHERE (:skuId IS NULL OR sh.skuId = :skuId)
        AND (:from IS NULL OR sh.createdAt >= :from)
        AND (:to IS NULL OR sh.createdAt <= :to)
        ORDER BY sh.createdAt DESC
    """)
    Page<ReceiveHistory> search(
            @Param("skuId") Long skuId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
