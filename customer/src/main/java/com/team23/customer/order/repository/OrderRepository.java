package com.team23.customer.order.repository;

import com.team23.customer.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 주문번호로 조회 (간단).
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 주문번호로 조회 (OrderItem 함께).
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items
            WHERE o.orderNumber = :orderNumber
            """)
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    /**
     * 회원의 모든 주문 (페이지네이션).
     * 목록 조회라 OrderItem fetch 안 함 (Pageable + Collection fetch 함정 회피).
     */
    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 회원의 주문 ID로 조회 (보안 검증).
     * OrderItem 함께 fetch.
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items
            WHERE o.id = :orderId AND o.memberId = :memberId
            """)
    Optional<Order> findByIdAndMemberIdWithItems(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );
}