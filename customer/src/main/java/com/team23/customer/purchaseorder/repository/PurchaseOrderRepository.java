package com.team23.customer.purchaseorder.repository;

import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPurchaseOrderNumber(String purchaseOrderNumber);

    Page<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);   // 발주 내역 조회용 (다음 이슈)

    Page<PurchaseOrder> findBySkuIdOrderByCreatedAtDesc(Long skuId, Pageable pageable);   // SKU별 조회 (다음 이슈)
}
