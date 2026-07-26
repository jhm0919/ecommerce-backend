package com.shop.product.repository;

import com.shop.product.domain.Sku;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    Optional<Sku> findBySkuCode(String skuCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM Sku s
        JOIN FETCH s.product p
        WHERE s.id = :skuId
          AND p.id = :productId
    """)
    Optional<Sku> findByIdAndProductIdForUpdate(
            @Param("skuId") Long skuId,
            @Param("productId") Long productId
    );
}
