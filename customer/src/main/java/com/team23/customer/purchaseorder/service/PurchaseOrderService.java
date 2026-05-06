package com.team23.customer.purchaseorder.service;

import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.repository.SkuRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.customer.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.team23.customer.purchaseorder.dto.PurchaseOrderListResponse;
import com.team23.customer.purchaseorder.exception.PurchaseOrderException;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SkuRepository skuRepository;

    public PurchaseOrder create(Long skuId,
                                int quantity,
                                String supplierName,
                                String supplierContact,
                                LocalDate expectedAt) {
        // 1. SKU 조회 + 존재 검증
        SKU sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new PurchaseOrderException(
                        ErrorCode.SKU_NOT_FOUND, "skuId=" + skuId));

        // 2. DISCONTINUED 차단
        Product product = sku.getProduct();   // Lazy 발동 — @Transactional 안이라 OK
        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new PurchaseOrderException(
                    ErrorCode.PRODUCT_DISCONTINUED, "skuId=" + skuId);
        }

        // 3. 수량 검증은 PurchaseOrder.create() 도메인 안에서 처리
        PurchaseOrder purchaseOrder = PurchaseOrder.create(
                skuId, quantity, supplierName, supplierContact, expectedAt);

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderListResponse> search(
            PurchaseOrderStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to != null)   ? to.atTime(23, 59, 59) : null;

        return purchaseOrderRepository
                .search(status, fromDt, toDt, pageable)
                .map(PurchaseOrderListResponse::from);
    }
}
