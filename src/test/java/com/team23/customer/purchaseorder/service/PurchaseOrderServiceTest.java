package com.team23.customer.purchaseorder.service;

import com.team23.customer.product.domain.*;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.customer.purchaseorder.dto.PurchaseOrderListResponse;
import com.team23.customer.purchaseorder.exception.PurchaseOrderException;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PurchaseOrderServiceTest {
    @Autowired PurchaseOrderService purchaseOrderService;
    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    EntityManager em;

    Long skuId = 0L;

    @BeforeEach
    void setUp() {
        // given - SKU (팀원 Product + SKU 구조로 만들기)
        Category category = categoryRepository.save(Category.create("의류", "t-shirt"));
        Product product = Product.register("티셔츠", new Money(BigDecimal.valueOf(10000), "KRW"), "설명", "url", category);
        productRepository.save(product);

        // SKU 추가 (Product 통해서)
        List<SkuOption> options = List.of(new SkuOption("색상", "blue"));
        SKU sku = product.addSku(options, 100);
        Product saved = productRepository.save(product);// cascade로 sku 저장

        em.flush();
        em.clear();

        this.skuId = saved.getSkus().get(0).getId();
    }

    @Test
    @DisplayName("정상 발주 생성")
    @Transactional
    void createSuccess() {
        // when
        PurchaseOrder po = purchaseOrderService.create(
                skuId, 100, "ABC 공급사", "010-1234-5678", LocalDate.now().plusDays(7)
        );

        // then
        assertThat(po.getId()).isNotNull();
        assertThat(po.getPurchaseOrderNumber()).startsWith("PO-");
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.REQUESTED);
    }

    @Test
    @DisplayName("없는 skuId → PurchaseOrderException")
    @Transactional
    void createSkuNotFoundThrowsException() {
        assertThatThrownBy(() ->
                purchaseOrderService.create(99999L, 10, "공급사", null, LocalDate.now().plusDays(7))
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("전체 조회 — 페이지네이션")
    @Transactional
    void searchAll() {
        // given - 발주 3개 직접 저장
        purchaseOrderRepository.save(PurchaseOrder.create(
                1L, 100, "공급사A", null, LocalDate.now().plusDays(7)));
        purchaseOrderRepository.save(PurchaseOrder.create(
                1L, 50,  "공급사B", null, LocalDate.now().plusDays(14)));

        // when
        Page<PurchaseOrderListResponse> result =
                purchaseOrderService.search(null, null, null,
                        PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("상태 필터 — REQUESTED 만")
    @Transactional
    void searchByStatus() {
        purchaseOrderRepository.save(PurchaseOrder.create(
                1L, 100, "공급사A", null, LocalDate.now().plusDays(7)));
        PurchaseOrder cancelled = PurchaseOrder.create(
                2L, 50, "공급사B", null, LocalDate.now().plusDays(14));
        cancelled.cancel();
        purchaseOrderRepository.save(cancelled);

        Page<PurchaseOrderListResponse> result =
                purchaseOrderService.search(
                        PurchaseOrderStatus.REQUESTED, null, null,
                        PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status())
                .isEqualTo(PurchaseOrderStatus.REQUESTED);
    }

    @Test
    @DisplayName("기간 필터")
    @Transactional
    void searchByDateRange() {
        // given - 발주 3개 직접 저장
        purchaseOrderRepository.save(PurchaseOrder.create(
                1L, 100, "공급사A", null, LocalDate.now().plusDays(7)));
        purchaseOrderRepository.save(PurchaseOrder.create(
                1L, 50,  "공급사B", null, LocalDate.now().plusDays(14)));

        // when
        Page<PurchaseOrderListResponse> result =
                purchaseOrderService.search(null, LocalDate.now(), LocalDate.now(),
                        PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
