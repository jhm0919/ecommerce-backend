package com.team23.customer.product.repository;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category c
            WHERE p.status <> :excludedStatus
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:keyword IS NULL OR
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> findVisibleProducts(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
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

    boolean existsByCategoryId(Long categoryId);
}
