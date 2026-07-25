package com.shop.order.repository;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatus(OrderStatus status);

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

    /**
     * 일별 매출 집계 (PENDING + CONFIRMED 만, CANCELLED 제외).
     *
     * @return Object[] = [date(java.sql.Date), revenue(BigDecimal), count(Long)]
     */
    @Query("""
          SELECT oi
          FROM Order o
          JOIN o.items oi
          WHERE o.status IN :statuses
            AND o.createdAt >= :from
            AND o.createdAt < :toExclusive
          ORDER BY oi.productName ASC, oi.price ASC
      """)
    List<OrderItem> findDailySalesItems( // OrderItem 엔티티만 반환
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive,
            @Param("statuses") List<OrderStatus> statuses
    );



    /**
     * 월별 매출 집계 (PENDING + CONFIRMED 만, CANCELLED 제외).
     *
     * @return Object[] = [yearMonth(String "YYYY-MM"), revenue(BigDecimal), count(Long)]
     */
    @Query("""
          SELECT (
               FUNCTION('DATE', o.createdAt),
               COALESCE(SUM(o.totalPrice), 0)
           )
           FROM Order o
           WHERE o.status IN :statuses
             AND o.createdAt >= :from
             AND o.createdAt < :toExclusive
           GROUP BY FUNCTION('DATE', o.createdAt)
           ORDER BY FUNCTION('DATE', o.createdAt) ASC
      """)
    List<Object[]> findMonthlySalesItems(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive,
            @Param("statuses") List<OrderStatus> statuses
    );

    List<Order> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
}