package com.team23.customer.product.repository;

import com.team23.customer.product.domain.SKU;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkuRepository extends JpaRepository<SKU, Long> {

    Optional<SKU> findBySkuCode(String skuCode);
}
