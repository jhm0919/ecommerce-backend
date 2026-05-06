package com.team23.customer.purchaseorder.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 발주서 Aggregate Root.
 *
 * 판매자가 재고 보충을 위해 공급처에 발주하는 문서.
 * SKU 단위로 발주하며, 입고 처리 후 재고가 증가한다.
 */
@Entity
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_po_number", columnList = "purchase_order_number", unique = true),
        @Index(name = "idx_po_sku", columnList = "sku_id"),
        @Index(name = "idx_po_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_order_number",
            nullable = false, unique = true, updatable = false, length = 30)
    private String purchaseOrderNumber;

    @Column(name = "sku_id", nullable = false, updatable = false)
    private Long skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "supplier_contact", length = 100)
    private String supplierContact;   // 선택 입력

    @Column(name = "expected_at", nullable = false)
    private LocalDate expectedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static PurchaseOrder create(
            Long skuId,
            int quantity,
            String supplierName,
            String supplierContact,
            LocalDate expectedAt
    ) {
        validateQuantity(quantity);
        validateSupplierName(supplierName);
        Objects.requireNonNull(expectedAt, "expectedAt must not be null");

        PurchaseOrder po = new PurchaseOrder();
        po.purchaseOrderNumber = PurchaseOrderNumberGenerator.generate();
        po.skuId = skuId;
        po.quantity = quantity;
        po.supplierName = supplierName.trim();
        po.supplierContact = supplierContact;
        po.expectedAt = expectedAt;
        po.status = PurchaseOrderStatus.REQUESTED;
        return po;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    public void cancel() {
        if (!status.isCancellable()) {
            throw new IllegalStateException(
                    "Cannot cancel: status is " + status);
        }
        this.status = PurchaseOrderStatus.CANCELLED;
    }

    public void receive() {
        if (this.status != PurchaseOrderStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Cannot receive: status is " + status);
        }
        this.status = PurchaseOrderStatus.RECEIVED;
    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "발주 수량은 1 이상이어야 합니다: " + quantity);
        }
    }

    private static void validateSupplierName(String supplierName) {
        Objects.requireNonNull(supplierName, "supplierName must not be null");
        if (supplierName.isBlank()) {
            throw new IllegalArgumentException("supplierName must not be blank");
        }
    }
}
