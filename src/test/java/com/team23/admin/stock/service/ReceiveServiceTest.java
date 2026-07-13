package com.team23.admin.stock.service;

import com.team23.category.domain.Category;
import com.team23.category.repository.CategoryRepository;
import com.team23.product.domain.Product;
import com.team23.product.domain.Sku;
import com.team23.product.domain.SkuOption;
import com.team23.product.repository.ProductRepository;
import com.team23.admin.purchaseorder.domain.PurchaseOrder;
import com.team23.admin.purchaseorder.domain.PurchaseOrderStatus;
import com.team23.admin.purchaseorder.dto.ReceiveCancelResponse;
import com.team23.admin.purchaseorder.exception.PurchaseOrderException;
import com.team23.admin.purchaseorder.repository.PurchaseOrderRepository;
import com.team23.admin.stock.domain.ReceiveHistory;
import com.team23.admin.stock.dto.ReceiveAdjustResponse;
import com.team23.admin.stock.dto.ReceiveStockResponse;
import com.team23.admin.stock.dto.ReceiveHistoryResponse;
import com.team23.admin.stock.repository.ReceiveHistoryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReceiveServiceTest {
    @Autowired
    ReceiveService receiveService;
    @Autowired PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    ReceiveHistoryRepository receiveHistoryRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired EntityManager em;

    Long skuId = 0L;

    @BeforeEach
    void setUp() {
        // given - SKU (팀원 Product + SKU 구조로 만들기)
        Category category = categoryRepository.save(Category.create("신발", "sho"));
        Product product = Product.register("운동화", BigDecimal.valueOf(10000), "설명", "url", category);
        productRepository.save(product);

        // SKU 추가 (Product 통해서)
        List<SkuOption> options = List.of(new SkuOption("색상", "blue"));
        Sku sku = product.addSku(options, 100);
        Product saved = productRepository.save(product);// cascade로 sku 저장

        em.flush();
        em.clear();

        this.skuId = saved.getSkuses().get(0).getId();
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
                receiveService.receive(po.getId(), 50);

        // then
        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(response.receivedQuantity()).isEqualTo(50);
        assertThat(response.currentStock()).isGreaterThan(0);

        // 이력 기록 확인
        List<ReceiveHistory> histories = receiveHistoryRepository.findAll();

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
        receiveService.receive(po.getId(), 50);   // 1차

        assertThatThrownBy(() ->
                receiveService.receive(po.getId(), 50)   // 2차
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("없는 purchaseOrderId → 예외")
    void receiveNotFoundThrowsException() {
        assertThatThrownBy(() ->
                receiveService.receive(99999L, 50)
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("전체 조회")
    void searchAll() {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));
        receiveHistoryRepository.save(ReceiveHistory.of(2L, 2L, 50,  150));

        // when
        Page<ReceiveHistoryResponse> result =
                receiveService.search(null, null, null,
                        PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("SKU 필터")
    void searchBySkuId() {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));
        receiveHistoryRepository.save(ReceiveHistory.of(2L, 2L, 50,  150));

        // when
        Page<ReceiveHistoryResponse> result =
                receiveService.search(1L, null, null,
                        PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).skuId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("기간 필터 — 오늘")
    void searchByDateRange() {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));

        // when
        Page<ReceiveHistoryResponse> result =
                receiveService.search(
                        null,
                        LocalDate.now(),
                        LocalDate.now(),
                        PageRequest.of(0, 20)
                );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간 필터 — 범위 밖 → 결과 없음")
    void searchOutOfRange() {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 1L, 100, 200));

        // when
        Page<ReceiveHistoryResponse> result =
                receiveService.search(
                        null,
                        LocalDate.now().minusDays(2),
                        LocalDate.now().minusDays(1),   // 어제까지
                        PageRequest.of(0, 20)
                );

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("응답 필드 확인")
    void searchResponseFields() {
        // given
        receiveHistoryRepository.save(ReceiveHistory.of(1L, 5L, 80, 180));

        // when
        Page<ReceiveHistoryResponse> result =
                receiveService.search(null, null, null,
                        PageRequest.of(0, 20));

        // then
        ReceiveHistoryResponse response = result.getContent().get(0);
        assertThat(response.skuId()).isEqualTo(1L);
        assertThat(response.purchaseOrderId()).isEqualTo(5L);
        assertThat(response.receivedQuantity()).isEqualTo(80);
        assertThat(response.stockAfter()).isEqualTo(180);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("정상 취소 — 재고 차감 + 발주 복구")
    void cancelSuccess() {
        PurchaseOrder po = purchaseOrderRepository.save(
                PurchaseOrder.create(skuId, 50, "공급사", null,
                        LocalDate.now().plusDays(7))
        );
        po.receive();
        purchaseOrderRepository.save(po);

        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, po.getId(), 50, 150)
        );

        // when
        ReceiveCancelResponse response =
                receiveService.cancel(history.getId());

        // then
        assertThat(response.purchaseOrderStatus())
                .isEqualTo(PurchaseOrderStatus.REQUESTED);
        assertThat(receiveHistoryRepository
                .findById(history.getId()).orElseThrow().isCancelled())
                .isTrue();
    }

    @Test
    @DisplayName("없는 이력 ID → 예외")
    void cancelNotFoundThrowsException() {
        assertThatThrownBy(() -> receiveService.cancel(99999L))
                .isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("이미 취소된 이력 → 예외")
    void cancelAlreadyCancelledThrowsException() {
        // given
        PurchaseOrder po = purchaseOrderRepository.save(
                PurchaseOrder.create(skuId, 50, "공급사", null,
                        LocalDate.now().plusDays(7)));

        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, po.getId(), 50, 150));

        history.cancel();
        receiveHistoryRepository.saveAndFlush(history);   // ← saveAndFlush

        assertThatThrownBy(() ->
                receiveService.cancel(history.getId())
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("수량 감소 수정 — 재고 차감")
    void adjustDecreaseSuccess() {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 100, 200));

        ReceiveAdjustResponse response =
                receiveService.adjust(history.getId(), 80, "수량 상이");

        assertThat(response.originalQuantity()).isEqualTo(100);
        assertThat(response.adjustedQuantity()).isEqualTo(80);
        assertThat(response.currentStock()).isEqualTo(80);   // 100 - 20
    }

    @Test
    @DisplayName("수량 증가 수정 — 재고 증가")
    void adjustIncreaseSuccess() {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 80, 180));

        ReceiveAdjustResponse response =
                receiveService.adjust(history.getId(), 100, "수량 상이");

        assertThat(response.currentStock()).isEqualTo(120);  // 100 + 20
    }

    @Test
    @DisplayName("재고 부족 → 예외")
    void adjustInsufficientStockThrowsException() {
        // 현재 재고 100, 차감 필요 150 → 불가
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 250, 350));

        assertThatThrownBy(() ->
                receiveService.adjust(history.getId(), 100, "대폭 수정")
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("취소된 이력 수정 → 예외")
    void adjustCancelledThrowsException() {
        ReceiveHistory history = receiveHistoryRepository.save(
                ReceiveHistory.of(skuId, 1L, 100, 200));
        history.cancel();
        receiveHistoryRepository.saveAndFlush(history);

        assertThatThrownBy(() ->
                receiveService.adjust(history.getId(), 80, "사유")
        ).isInstanceOf(PurchaseOrderException.class);
    }

    @Test
    @DisplayName("없는 ID → 예외")
    void adjustNotFoundThrowsException() {
        assertThatThrownBy(() ->
                receiveService.adjust(99999L, 80, "사유")
        ).isInstanceOf(PurchaseOrderException.class);
    }
}
