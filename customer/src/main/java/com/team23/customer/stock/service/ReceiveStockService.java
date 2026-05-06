package com.team23.customer.stock.service;

import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.product.repository.SkuRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.customer.purchaseorder.exception.PurchaseOrderException;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import com.team23.customer.stock.domain.StockHistory;
import com.team23.customer.stock.dto.ReceiveStockResponse;
import com.team23.customer.stock.dto.StockHistoryResponse;
import com.team23.customer.stock.repository.StockHistoryRepository;
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
public class ReceiveStockService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public ReceiveStockResponse receive(Long purchaseOrderId, int receivedQuantity) {
        // 1. 발주 조회
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new PurchaseOrderException(
                        ErrorCode.PURCHASE_ORDER_NOT_FOUND,
                        "purchaseOrderId=" + purchaseOrderId));

        // 2. 상태 검증 — REQUESTED 만 입고 가능
        if (po.getStatus() != PurchaseOrderStatus.REQUESTED) {
            throw new PurchaseOrderException(
                    ErrorCode.INVALID_PURCHASE_ORDER_STATUS,
                    "현재 상태: " + po.getStatus());
        }

        // 3. SKU 조회
        SKU sku = skuRepository.findById(po.getSkuId())
                .orElseThrow(() -> new PurchaseOrderException(ErrorCode.SKU_NOT_FOUND, "skuId=" + po.getSkuId()));

        // 4. Product 조회 + 재고 증가 (SOLD_OUT → ACTIVE 자동 전환)
        Product product = productRepository.findById(sku.getProduct().getId())   // Lazy 발동 — @Transactional 안이라 OK
                .orElseThrow();

        product.increaseSkuStock(po.getSkuId(), receivedQuantity); // SKU 재고 증가 + SOLD_OUT→ACTIVE 감지

        // 5. 발주 상태 → RECEIVED
        po.receive();

        // 6. 재고 이력 기록
        int currentStock = sku.getStock();
        StockHistory history = StockHistory.of(
                po.getSkuId(),
                purchaseOrderId,
                receivedQuantity,
                currentStock
        );
        stockHistoryRepository.save(history);

        return new ReceiveStockResponse(
                po.getId(),
                po.getSkuId(),
                receivedQuantity,
                currentStock,
                po.getStatus()
        );

    }

    @Transactional(readOnly = true)
    public Page<StockHistoryResponse> search(
            Long skuId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to != null) ? to.atTime(23, 59, 59) : null;

        return stockHistoryRepository
                .search(skuId, fromDt, toDt, pageable)
                .map(StockHistoryResponse::from);
    }
}
