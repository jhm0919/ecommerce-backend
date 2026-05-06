package com.team23.customer.purchaseorder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PurchaseOrderTest {
    @Test
    @DisplayName("정상 생성")
    void create_success() {
        PurchaseOrder po = PurchaseOrder.create(
                1L, 100, "ABC 공급사", null, LocalDate.now().plusDays(7)
        );

        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.REQUESTED);
        assertThat(po.getPurchaseOrderNumber()).startsWith("PO-");
        assertThat(po.getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("수량 0 이하 → 예외")
    void createInvalidQuantityThrowsException() {

        assertThatThrownBy(() ->
                PurchaseOrder.create(1L, 0, "공급사", null, LocalDate.now().plusDays(7))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 이상");
    }

    @Test
    @DisplayName("공급처명 공백 → 예외")
    void createBlankSupplierNameThrowsException() {
        assertThatThrownBy(() ->
                PurchaseOrder.create(1L, 1, "", null, LocalDate.now().plusDays(7))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("공백");
    }

    @Test
    @DisplayName("reopen — RECEIVED → REQUESTED")
    void reopenSuccess() {
        PurchaseOrder po = PurchaseOrder.create(
                1L, 100, "공급사", null, LocalDate.now().plusDays(7));
        po.receive();   // RECEIVED 상태로

        po.reopen();

        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.REQUESTED);
    }

    @Test
    @DisplayName("REQUESTED 상태에서 reopen → 예외")
    void reopenNotReceivedThrowsException() {
        PurchaseOrder po = PurchaseOrder.create(
                1L, 100, "공급사", null, LocalDate.now().plusDays(7));

        assertThatThrownBy(po::reopen)
                .isInstanceOf(IllegalStateException.class);
    }
}