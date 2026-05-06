package com.team23.customer.stock.repository;

import com.team23.customer.stock.domain.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    Page<StockHistory> findBySkuIdOrderByCreatedAtDesc(Long skuId, Pageable pageable);

    Page<StockHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT sh FROM StockHistory sh
        WHERE (:skuId IS NULL OR sh.skuId = :skuId)
        AND (:from IS NULL OR sh.createdAt >= :from)
        AND (:to IS NULL OR sh.createdAt <= :to)
        ORDER BY sh.createdAt DESC
    """)
    Page<StockHistory> search(
            @Param("skuId") Long skuId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
