package com.team23.customer.stock.service;

import com.team23.customer.product.domain.*;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.purchaseorder.domain.PurchaseOrder;
import com.team23.customer.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.customer.purchaseorder.exception.PurchaseOrderException;
import com.team23.customer.purchaseorder.repository.PurchaseOrderRepository;
import com.team23.customer.stock.domain.StockHistory;
import com.team23.customer.stock.dto.ReceiveStockResponse;
import com.team23.customer.stock.repository.StockHistoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class StockServiceTest {
    @Autowired
    StockService stockService;
    @Autowired PurchaseOrderRepository purchaseOrderRepository;
    @Autowired StockHistoryRepository stockHistoryRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired EntityManager em;

    @AfterEach
    void cleanUp() {
        stockHistoryRepository.deleteAll();
        purchaseOrderRepository.deleteAll();
    }

    Long skuId = 0L;

    @BeforeEach
    void setUp() {
        // given - SKU (팀원 Product + SKU 구조로 만들기)
        Category category = categoryRepository.save(Category.create("신발", "sho"));
        Product product = Product.register("운동화", new Money(BigDecimal.valueOf(10000), "KRW"), "설명", "url", category);
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
    @DisplayName("정상 입고 처리 — 재고 증가 + 이력 기록")
    void receiveSuccess() {

        PurchaseOrder po = purchaseOrderRepository.save(
                PurchaseOrder.create(skuId, 50, "공급사A", null,
                        LocalDate.now().plusDays(7))
        );

        // when
        ReceiveStockResponse response =
                stockService.receive(po.getId(), 50);

        // then
        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(response.receivedQuantity()).isEqualTo(50);
        assertThat(response.currentStock()).isGreaterThan(0);

        // 이력 기록 확인
        List<StockHistory> histories = stockHistoryRepository.findAll();

        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getReceivedQuantity()).isEqualTo(50);
        assertThat(histories.get(0).getPurchaseOrderId()).isEqualTo(po.getId());
    }

    @Test
    @DisplayName("RECEIVED 발주 재입고 시도 → 예외")
    void receiveAlreadyReceivedThrowsException() {
        PurchaseOrder po = purchaseOrderRepository.save(
                PurchaseOrder.create(skuId, 50, "공급사A", null,
                        LocalDate.now().plusDays(7))
        );
        stockService.receive(po.getId(), 50);   // 1차

        assertThatThrownBy(() ->
                stockService.receive(po.getId(), 50)   // 2차
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("없는 purchaseOrderId → 예외")
    void receiveNotFoundThrowsException() {
        assertThatThrownBy(() ->
                stockService.receive(99999L, 50)
        ).isInstanceOf(PurchaseOrderException.class);
    }
}