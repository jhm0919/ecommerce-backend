package com.team23.admin.purchaseorder.repository;

import com.team23.admin.purchaseorder.domain.PurchaseOrder;
import com.team23.admin.purchaseorder.domain.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPurchaseOrderNumber(String purchaseOrderNumber);

    Page<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);   // 발주 내역 조회용 (다음 이슈)

    Page<PurchaseOrder> findBySkuIdOrderByCreatedAtDesc(Long skuId, Pageable pageable);   // SKU별 조회 (다음 이슈)

    @Query("""
        SELECT po FROM PurchaseOrder po
        WHERE (:status IS NULL OR po.status = :status)
        AND (:from IS NULL OR po.createdAt >= :from)
        AND (:to IS NULL OR po.createdAt <= :to)
        ORDER BY po.createdAt DESC
    """)
    Page<PurchaseOrder> search(
            @Param("status") PurchaseOrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
