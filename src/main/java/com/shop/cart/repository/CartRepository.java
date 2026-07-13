package com.shop.cart.repository;

import com.shop.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 회원의 장바구니 조회.
     */
    Optional<Cart> findByMemberId(Long memberId);

    /**
     * 회원의 장바구니 조회 (CartItem 함께 fetch).
     * 조회 후 항목 정보까지 사용할 때.
     */
    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.items
            WHERE c.memberId = :memberId
            """)
    Optional<Cart> findByMemberIdWithItems(@Param("memberId") Long memberId);

    @Query("""
        SELECT COUNT(c)
        FROM Cart c
        WHERE EXISTS (
            SELECT 1
            FROM CartItem ci
            WHERE ci.cart = c
        )
    """)
    long countNonEmptyCarts();

    /**
     * 특정 상품 ID를 포함하는 모든 장바구니 항목을 삭제한다.
     * 상품 단종 시 호출된다.
     * @param productId 단종된 상품의 ID
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productId = :productId")
    void deleteAllItemsByProductId(@Param("productId") Long productId);
}