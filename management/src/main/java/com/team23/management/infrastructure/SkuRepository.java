package com.team23.management.infrastructure;

import com.team23.management.domain.sku.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {
    List<Sku> findByProductId(Long productId);
    @Query("SELECT s FROM Sku s LEFT JOIN FETCH s.options WHERE s.productId = :productId")
    List<Sku> findByProductIdWithOptions(@Param("productId") Long productId);

    long countByProductId(Long productId);
}
