package com.team23.management.infrastructure;

import com.team23.management.domain.sku.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {
    List<Sku> findByProductId(Long productId);
}
