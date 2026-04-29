package com.team23.management.infrastructure;

import com.team23.management.domain.Stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
