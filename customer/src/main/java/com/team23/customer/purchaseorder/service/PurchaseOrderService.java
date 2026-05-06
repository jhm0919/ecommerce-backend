package com.team23.customer.purchaseorder.service;

import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.repository.SkuRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.dto.PurchaseOrderRequest;
import com.team23.customer.purchaseorder.exception.PurchaseOrderException;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SkuRepository skuRepository;

    public PurchaseOrder create(PurchaseOrderRequest request) {
        // 1. SKU 조회 + 존재 검증
        SKU sku = skuRepository.findById(request.skuId())
                .orElseThrow(() -> new PurchaseOrderException(ErrorCode.SKU_NOT_FOUND, "skuId=" + request.skuId()));

        // 2. DISCONTINUED 차단
        Product product = sku.getProduct();   // Lazy 발동 — @Transactional 안이라 OK
        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new PurchaseOrderException(ErrorCode.PRODUCT_DISCONTINUED, "skuId=" + request.skuId());
        }

        // 3. 수량 검증은 PurchaseOrder.create() 도메인 안에서 처리
        PurchaseOrder purchaseOrder = PurchaseOrder.create(
                request.skuId(),
                request.quantity(),
                request.supplierName(),
                request.supplierContact(),
                request.expectedAt());

        return purchaseOrderRepository.save(purchaseOrder);
    }
}
