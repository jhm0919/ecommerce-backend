package com.team23.customer.order.repository;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderAdminRepository extends JpaRepository<Order, Long> {
    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status = :status)
        AND (:from IS NULL OR o.createdAt >= :from)
        AND (:to IS NULL OR o.createdAt <= :to)
        ORDER BY o.createdAt DESC
    """)
    Page<Order> search(
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND (:from IS NULL OR o.createdAt >= :from)
        AND (:to IS NULL OR o.createdAt <= :to)
        ORDER BY o.createdAt DESC
    """)
    List<Order> searchByStatus(
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
