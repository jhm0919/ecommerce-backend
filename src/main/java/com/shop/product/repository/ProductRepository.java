package com.shop.product.repository;

import com.shop.product.domain.Product;
import com.shop.product.domain.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 사용자 노출용 상품 목록 조회 (DISCONTINUED 제외).
     * <p>
     * 카테고리 필터와 검색어가 모두 선택적:
     * <p>
     * {@code categoryId}가 null이면 카테고리 필터 적용 X
     * {@code keyword}가 null이면 검색 적용 X
     * <p>
     * 검색어는 상품명에서 부분 매칭 (대소문자 무시).
     */
    @Query(
            """
                SELECT DISTINCT p
                FROM Product p
                JOIN p.category c
                WHERE p.status <> :excludedStatus
                  AND (:categoryId IS NULL OR c.id = :categoryId)
                  AND (:keyword IS NULL OR
                       LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                """
    )
    Page<Product> findProductList(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("excludedStatus") ProductStatus excludedStatus,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT p
                FROM Product p
                JOIN FETCH p.skus
                WHERE p.id = :productId
    """)
    Optional<Product> findByIdWithPessimistic(@Param("productId") Long productId);

    @Query("""
                SELECT DISTINCT p
                FROM Product p
                JOIN FETCH p.skus
                WHERE p.id = :productId
            """)
    Optional<Product> findByIdWithSkus(@Param("productId") Long productId);


    boolean existsByCategoryId(Long categoryId);

}
