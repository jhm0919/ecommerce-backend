package com.team23.management.infrastructure;

import com.team23.management.domain.Sku.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {
}
