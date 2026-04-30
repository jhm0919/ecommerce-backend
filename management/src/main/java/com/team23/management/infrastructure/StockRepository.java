package com.team23.management.infrastructure;

import com.team23.management.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findBySkuId(Long skuId);                    // 단일 (N+1 발생용)
    List<Stock> findBySkuIdIn(List<Long> skuIds);
}
