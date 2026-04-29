package com.team23.customer.product.repository;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 사용자 노출용 상품 목록 조회 (DISCONTINUED 제외).
     * Category를 함께 fetch하여 N+1 문제 방지.
     */
    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category
            WHERE p.status <> :excludedStatus
            """)
    Page<Product> findVisibleProducts(
            @Param("excludedStatus") ProductStatus excludedStatus,
            Pageable pageable
    );

    /**
     * 특정 카테고리의 노출용 상품 목록.
     */
    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category c
            WHERE p.status <> :excludedStatus
              AND c.id = :categoryId
            """)
    Page<Product> findVisibleProductsByCategory(
            @Param("categoryId") Long categoryId,
            @Param("excludedStatus") ProductStatus excludedStatus,
            Pageable pageable
    );

    /**
     * 상품 상세 조회 (Category 함께).
     */
    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithCategory(@Param("id") Long id);
}
