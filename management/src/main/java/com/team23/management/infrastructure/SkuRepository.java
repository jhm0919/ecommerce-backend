package com.team23.management.infrastructure;

import com.team23.management.domain.sku.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {
}
