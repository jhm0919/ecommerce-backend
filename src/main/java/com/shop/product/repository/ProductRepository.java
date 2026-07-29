package com.shop.product.repository;

import com.shop.product.domain.Product;
import com.shop.product.domain.ProductStatus;
import com.shop.product.dto.ProductSummaryProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 사용자 노출용 상품 목록 조회 (DISCONTINUED 제외).
     *
     * <p>카테고리 필터와 검색어가 모두 선택적:
     * <ul>
     *   <li>{@code categoryId}가 null이면 카테고리 필터 적용 X</li>
     *   <li>{@code keyword}가 null이면 검색 적용 X</li>
     * </ul>
     *
     * <p>검색어는 상품명과 설명에서 부분 매칭 (대소문자 무시).
     *
     * <p>Category를 함께 fetch하여 N+1 문제 방지.
     */
    @Query(
            value = """
            SELECT new com.shop.product.dto.ProductSummaryProjection(
                p.id,
                p.name,
                p.price,
                p.mainImageUrl,
                c.name,
                p.status,
                COALESCE(SUM(s.quantity), 0)
            )
            FROM Product p
            JOIN p.category c
            LEFT JOIN p.skus s
            WHERE p.status <> :excludedStatus
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:keyword IS NULL OR
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            GROUP BY p.id, p.name, p.price,
                     p.mainImageUrl, c.name, p.status, p.createdAt
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM Product p
            JOIN p.category c
            WHERE p.status <> :excludedStatus
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:keyword IS NULL OR
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """
    )
    Page<ProductSummaryProjection> findProductList(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("excludedStatus") ProductStatus excludedStatus,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      SELECT p
      FROM Product p
      LEFT JOIN FETCH p.skus s
      WHERE p.id = :productId
  """)
    Optional<Product> findByIdWithSkusForUpdate(@Param("productId") Long productId);

    /**
     * 상품 상세 조회 (Category 함께).
     */
    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN FETCH p.category
            LEFT JOIN FETCH p.skus
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);

    // 후보 상품 (재고 있는 최신 20개)
    @Query(
            value = """
            SELECT p.*
            FROM products p
            LEFT JOIN skus s ON s.product_id = p.id
            GROUP BY p.id
            HAVING COALESCE(SUM(s.stock), 0) > :stock
            ORDER BY p.created_at DESC
            LIMIT 20
            """,
            nativeQuery = true
    )
    List<Product> findTop20ByStockGreaterThanOrderByCreatedAtDesc(@Param("stock") int stock);

    // Fallback 용 (재고 있는 최신 3개)
    @Query(
            value = """
            SELECT p.*
            FROM products p
            LEFT JOIN skus s ON s.product_id = p.id
            GROUP BY p.id
            HAVING COALESCE(SUM(s.stock), 0) > :stock
            ORDER BY p.created_at DESC
            LIMIT 3
            """,
            nativeQuery = true
    )
    List<Product> findTop3ByStockGreaterThanOrderByCreatedAtDesc(@Param("stock") int stock);

}
