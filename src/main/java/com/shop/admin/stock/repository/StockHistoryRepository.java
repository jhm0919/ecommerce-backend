package com.shop.admin.stock.repository;

import com.shop.admin.stock.domain.StockType;
import com.shop.admin.stock.domain.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

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
            @Param("changeType") StockType changeType,
            Pageable pageable
    );

    @Query("""
            SELECT h FROM StockHistory h
            WHERE h.stockBefore > 0
              AND h.stockAfter = 0
              AND h.changeType IN :changeTypes
              AND h.occurredAt >= :from
            ORDER BY h.occurredAt DESC, h.id DESC
            """)
    Page<StockHistory> findSoldOutTransitionsSince(
            @Param("changeTypes") Collection<StockType> changeTypes,
            @Param("from") LocalDateTime from,
            Pageable pageable
    );
}
