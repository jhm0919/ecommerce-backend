package com.shop.product.repository;

import com.shop.product.domain.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    Optional<Sku> findBySkuCode(String skuCode);
}
