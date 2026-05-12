package com.team23.customer.order.repository;

import com.team23.customer.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 기간 내 인기 상품 Top 5.
     * PENDING 주문만 집계.
     * [productId, productName, totalQty, totalRevenue] 반환.
     */
    @Query("""
            SELECT
                oi.productId,
                oi.productName,
                SUM(oi.quantity),
                SUM(oi.priceAtOrder.amount * oi.quantity)
            FROM OrderItem oi
            JOIN Order o ON o.id = oi.order.id
            WHERE o.status = 'PENDING'
              AND o.createdAt >= :from
              AND o.createdAt <= :to
            GROUP BY oi.productId, oi.productName
            ORDER BY SUM(oi.quantity) DESC
            LIMIT 5
            """)
    List<Object[]> findTop5PopularProducts(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
