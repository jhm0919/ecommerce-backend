package com.team23.customer.settlement.service;

import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderAdmin;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.repository.OrderAdminRepository;
import com.team23.customer.product.domain.*;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.settlement.dto.SettlementSummaryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SettlementServiceTest {
    @Autowired
    SettlementService settlementService;
    @Autowired
    OrderAdminRepository orderAdminRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;

    private Product testProduct;
    private SKU testSku;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(
                Category.create("신발", "shoe-settlement"));
        Product product = Product.register(
                "운동화",
                new Money(BigDecimal.valueOf(10000), "KRW"),
                "설명", "url", category);
        productRepository.save(product);

        List<SkuOption> options = List.of(new SkuOption("색상", "blue"));
        product.addSku(options, 100);
        Product saved = productRepository.saveAndFlush(product);
        this.testProduct = saved;
        this.testSku = saved.getSkus().get(0);
    }

    @AfterEach
    void cleanUp() {
        orderAdminRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // 헬퍼 — CONFIRMED 주문 생성
    private Order createConfirmedOrder(int quantity) {
        OrderItem item = OrderItem.of(testProduct, testSku, quantity);
        Order order = Order.createForMember(1L, List.of(item));
        orderAdminRepository.save(order);
        OrderAdmin.confirm(order);
        return orderAdminRepository.saveAndFlush(order);
    }

    // 헬퍼 — PENDING 주문 생성
    private Order createPendingOrder() {
        OrderItem item = OrderItem.of(testProduct, testSku, 1);
        Order order = Order.createForMember(1L, List.of(item));
        return orderAdminRepository.saveAndFlush(order);
    }

    // 헬퍼 — CANCELLED 주문 생성
    private Order createCancelledOrder() {
        OrderItem item = OrderItem.of(testProduct, testSku, 1);
        Order order = Order.createForMember(1L, List.of(item));
        order.cancel();
        return orderAdminRepository.saveAndFlush(order);
    }

    @Test
    @DisplayName("CONFIRMED 주문만 정산 대상 포함")
    void searchSettlementOnlyIncludesConfirmedOrders() {
        createConfirmedOrder(1);   // 정산 대상
        createPendingOrder();      // 제외
        createCancelledOrder();    // 제외

        SettlementSummaryResponse response = settlementService.search(null, null);

        assertThat(response.items()).hasSize(1);
    }

    @Test
    @DisplayName("총액 - 수수료 = 정산금액 정합성 확인")
    void searchSettlementFeeCalculationIsCorrect() {
        createConfirmedOrder(1);   // 10,000원

        SettlementSummaryResponse response = settlementService.search(null, null);

        BigDecimal expectedFee = response.totalSalesAmount()
                .multiply(BigDecimal.valueOf(0.035))
                .setScale(0, RoundingMode.HALF_UP);

        assertThat(response.totalFee()).isEqualTo(expectedFee);
        assertThat(response.totalSettlementAmount())
                .isEqualTo(response.totalSalesAmount().subtract(response.totalFee()));
    }

    @Test
    @DisplayName("여러 주문 합산 — totalSalesAmount 정확")
    void searchSettlementSumOfMultipleOrders() {
        createConfirmedOrder(1);   // 10,000원
        createConfirmedOrder(2);   // 20,000원

        SettlementSummaryResponse response = settlementService.search(null, null);

        assertThat(response.items()).hasSize(2);
        assertThat(response.totalSalesAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(30000));
    }

    @Test
    @DisplayName("기간 필터 — 오늘 생성 주문만")
    void searchSettlementByDateRange() {
        createConfirmedOrder(1);

        SettlementSummaryResponse response = settlementService.search(
                        LocalDate.now(), LocalDate.now());

        assertThat(response.items()).hasSize(1);
    }

    @Test
    @DisplayName("기간 밖 — 결과 없음")
    void searchSettlementOutOfDateRangeReturnsEmpty() {
        createConfirmedOrder(1);

        SettlementSummaryResponse response = settlementService.search(
                        LocalDate.now().minusDays(2),
                        LocalDate.now().minusDays(1));

        assertThat(response.items()).isEmpty();
        assertThat(response.totalSalesAmount())
                .isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("정산 대상 없을 때 — 모든 합계 0")
    void searchSettlementNoOrdersReturnsZero() {
        SettlementSummaryResponse response = settlementService.search(null, null);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalSalesAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.totalFee()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.totalSettlementAmount()).isEqualTo(BigDecimal.ZERO);
    }
}