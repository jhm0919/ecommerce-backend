package com.team23.customer.stockhistory.repository;

import com.team23.customer.stockhistory.domain.StockChangeType;
import com.team23.customer.stockhistory.domain.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

    /**
     * Product 단위 이력 조회.
     * skuId, changeType 선택적 필터.
     */
    @Query("""
            SELECT h FROM StockHistory h
            WHERE h.productId = :productId
              AND (:skuId IS NULL OR h.skuId = :skuId)
              AND (:changeType IS NULL OR h.changeType = :changeType)
            ORDER BY h.occurredAt DESC
            """)
    Page<StockHistory> findHistories(
            @Param("productId") Long productId,
            @Param("skuId") Long skuId,
            @Param("changeType") StockChangeType changeType,
            Pageable pageable
    );
}
