package com.team23.customer.stock.repository;

import com.team23.customer.stock.domain.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    Page<StockHistory> findBySkuIdOrderByCreatedAtDesc(Long skuId, Pageable pageable);

    Page<StockHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
