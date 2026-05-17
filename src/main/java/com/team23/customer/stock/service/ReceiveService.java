package com.team23.customer.stock.service;

import com.team23.customer.member.exception.ErrorCode;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.product.repository.SkuRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.customer.purchaseorder.dto.ReceiveCancelResponse;
import com.team23.customer.purchaseorder.exception.PurchaseOrderException;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import com.team23.customer.stock.domain.ReceiveHistory;
import com.team23.customer.stock.dto.ReceiveAdjustResponse;
import com.team23.customer.stock.dto.ReceiveStockResponse;
import com.team23.customer.stock.dto.ReceiveHistoryResponse;
import com.team23.customer.stock.repository.ReceiveHistoryRepository;
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
public class ReceiveService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final ReceiveHistoryRepository receiveHistoryRepository;

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
        ReceiveHistory history = ReceiveHistory.of(
                po.getSkuId(),
                purchaseOrderId,
                receivedQuantity,
                currentStock
        );
        receiveHistoryRepository.save(history);

        return new ReceiveStockResponse(
                po.getId(),
                po.getSkuId(),
                receivedQuantity,
                currentStock,
                po.getStatus()
        );

    }

    @Transactional(readOnly = true)
    public Page<ReceiveHistoryResponse> search(
            Long skuId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt = (to != null) ? to.atTime(23, 59, 59) : null;

        return receiveHistoryRepository
                .search(skuId, fromDt, toDt, pageable)
                .map(ReceiveHistoryResponse::from);
    }

    public ReceiveCancelResponse cancel(Long receiveHistoryId) {
        // 1. 입고 이력 조회
        ReceiveHistory history = receiveHistoryRepository.findById(receiveHistoryId)
                .orElseThrow(() -> new PurchaseOrderException(
                        ErrorCode.RECEIVE_HISTORY_NOT_FOUND,
                        "id=" + receiveHistoryId));

        // 2. 이미 취소 검증
        if (history.isCancelled()) {
            throw new PurchaseOrderException(ErrorCode.ALREADY_CANCELLED);
        }

        // 3. SKU 조회
        SKU sku = skuRepository.findById(history.getSkuId())
                .orElseThrow(() -> new PurchaseOrderException(
                        ErrorCode.SKU_NOT_FOUND));

        // 4. 재고 부족 검증 (취소할 수량 > 현재 재고)
        if (sku.getStock() < history.getReceivedQuantity()) {
            throw new PurchaseOrderException(
                    ErrorCode.INSUFFICIENT_STOCK_FOR_CANCEL,
                    "현재 재고: " + sku.getStock() +
                            ", 취소 수량: " + history.getReceivedQuantity());
        }

        // 5. 재고 차감
        Product product = sku.getProduct();
        product.decreaseSkuStock(history.getSkuId(), history.getReceivedQuantity());

        // 6. 발주 상태 → REQUESTED 복구
        PurchaseOrder po = purchaseOrderRepository
                .findById(history.getPurchaseOrderId())
                .orElseThrow();
        po.reopen();

        // 7. 이력 취소 처리
        history.cancel();

        return new ReceiveCancelResponse(
                history.getId(),
                history.getSkuId(),
                history.getReceivedQuantity(),
                sku.getStock(),
                po.getStatus()
        );
    }

    public ReceiveAdjustResponse adjust(
            Long receiveHistoryId, int newQuantity, String reason
    ) {
        // 1. 입고 이력 조회
        ReceiveHistory history = receiveHistoryRepository
                .findById(receiveHistoryId)
                .orElseThrow(() -> new PurchaseOrderException(
                        ErrorCode.RECEIVE_HISTORY_NOT_FOUND,
                        "id=" + receiveHistoryId));

        // 2. 취소된 이력 수정 불가
        if (history.isCancelled()) {
            throw new PurchaseOrderException(ErrorCode.ALREADY_CANCELLED);
        }

        // 3. 차이 계산
        int originalQuantity = history.getCurrentQuantity();
        int diff = newQuantity - originalQuantity;   // 양수: 증가 / 음수: 차감

        // 4. 감소 방향 — 재고 부족 검증
        SKU sku = skuRepository.findById(history.getSkuId())
                .orElseThrow(() -> new PurchaseOrderException(
                        ErrorCode.SKU_NOT_FOUND));

        if (diff < 0 && sku.getStock() < Math.abs(diff)) {
            throw new PurchaseOrderException(
                    ErrorCode.INSUFFICIENT_STOCK_FOR_CANCEL,
                    "현재 재고: " + sku.getStock() +
                            ", 차감 필요: " + Math.abs(diff));
        }

        // 5. 재고 증감
        Product product = sku.getProduct();
        if (diff > 0) { // 재고 증가
            product.increaseSkuStock(history.getSkuId(), diff);
        } else if (diff < 0) { // 재고 차감 (부족 검증 필요)
            product.decreaseSkuStock(history.getSkuId(), Math.abs(diff));
        }
        // diff == 0 → 같은 수량 수정 (사유만 변경)

        // 6. 이력 수정
        history.adjust(newQuantity, reason);

        return new ReceiveAdjustResponse(
                history.getId(),
                history.getSkuId(),
                originalQuantity,
                newQuantity,
                sku.getStock(),
                reason
        );
    }
}