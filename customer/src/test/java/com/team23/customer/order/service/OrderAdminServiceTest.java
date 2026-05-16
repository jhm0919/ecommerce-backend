package com.team23.customer.order.service;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.dto.OrderAdminConfirmResponse;
import com.team23.customer.order.dto.OrderAdminListResponse;
import com.team23.customer.order.repository.OrderAdminRepository;
import com.team23.customer.product.domain.*;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
// ★ 클래스 레벨 @Transactional 제거 → 각 테스트 독립 트랜잭션
class OrderAdminServiceTest {

    @Autowired OrderAdminService orderAdminService;
    @Autowired OrderAdminRepository orderAdminRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired EntityManager entityManager;  // ★ 추가

    private Product testProduct;
    private SKU testSku;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(
                Category.create("신발", "shoe-order"));
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

    private Order createPendingOrder() {
        OrderItem item = OrderItem.of(testProduct, testSku, 1);
        Order order = Order.createForMember(1L, List.of(item));
        return orderAdminRepository.saveAndFlush(order);
    }

    private Order createCancelledOrder() {
        Order order = createPendingOrder();
        order.cancel();
        return orderAdminRepository.saveAndFlush(order);
    }

    @Test
    @DisplayName("전체 조회 — 모든 주문 반환")
    void searchAllReturnsAllOrders() {
        createPendingOrder();
        createPendingOrder();

        Page<OrderAdminListResponse> result =
                orderAdminService.search(null, null, null, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("상태 필터 PENDING — PENDING 주문만 반환")
    void searchByStatusPendingReturnsOnlyPending() {
        createPendingOrder();
        createCancelledOrder();

        Page<OrderAdminListResponse> result =
                orderAdminService.search(
                        OrderStatus.PENDING, null, null, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("기간 필터 — 범위 내 주문 반환")
    void searchByDateRangeReturnsOrdersInRange() {
        createPendingOrder();

        Page<OrderAdminListResponse> result =
                orderAdminService.search(
                        null, LocalDate.now(), LocalDate.now(), PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간 필터 — 범위 밖 주문 없음")
    void searchOutOfDateRangeReturnsEmpty() {
        createPendingOrder();

        Page<OrderAdminListResponse> result =
                orderAdminService.search(
                        null,
                        LocalDate.now().minusDays(2),
                        LocalDate.now().minusDays(1),
                        PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("응답 필드 — 필수 항목 포함 확인")
    void searchResponseFieldsContainsExpectedValues() {
        createPendingOrder();

        Page<OrderAdminListResponse> result =
                orderAdminService.search(null, null, null, PageRequest.of(0, 20));

        OrderAdminListResponse response = result.getContent().get(0);
        assertThat(response.orderId()).isNotNull();
        assertThat(response.orderNumber()).isNotBlank();
        assertThat(response.totalAmount()).isNotNull();
        assertThat(response.itemCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("PENDING 주문 일괄 확정 → CONFIRMED")
    void confirmPendingOrdersReturnsSuccessCount() {
        Order o1 = createPendingOrder();
        Order o2 = createPendingOrder();

        OrderAdminConfirmResponse response =
                orderAdminService.confirm(List.of(o1.getId(), o2.getId()));

        assertThat(response.successCount()).isEqualTo(2);
        assertThat(response.confirmedOrderIds())
                .containsExactlyInAnyOrder(o1.getId(), o2.getId());

        Order confirmed = orderAdminRepository.findById(o1.getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("없는 orderId → 예외 + 전체 롤백")
        // ★ @Transactional 없음 → 예외 후 DB 조회 가능
    void confirmWithNotFoundIdThrowsException() {
        Order o1 = createPendingOrder();

        assertThatThrownBy(() ->
                orderAdminService.confirm(List.of(o1.getId(), 99999L))
        ).isInstanceOf(BusinessException.class);

        // ★ 새 트랜잭션으로 조회 → rollback 여부 확인
        Order notChanged = orderAdminRepository.findById(o1.getId()).orElseThrow();
        assertThat(notChanged.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("CANCELLED 주문 확정 시도 → 예외 + 전체 롤백")
    void confirmCancelledOrderThrowsException() {
        Order pending = createPendingOrder();
        Order cancelled = createPendingOrder();
        cancelled.cancel();
        orderAdminRepository.saveAndFlush(cancelled);

        assertThatThrownBy(() ->
                orderAdminService.confirm(List.of(pending.getId(), cancelled.getId()))
        ).isInstanceOf(BusinessException.class);

        Order notChanged = orderAdminRepository.findById(pending.getId()).orElseThrow();
        assertThat(notChanged.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("단건 확정 → CONFIRMED")
    void confirmSingleOrderChangesStatusToConfirmed() {
        Order order = createPendingOrder();

        OrderAdminConfirmResponse response =
                orderAdminService.confirm(List.of(order.getId()));

        assertThat(response.successCount()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("정상 강제 취소 — CANCELLED + 재고 복구")
    void cancelOrderChangesStatusAndRestoresStock() {
        int orderQuantity = 2;

        Product freshProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        SKU freshSku = freshProduct.getSkus().get(0);
        int stockBefore = freshSku.getStock();

        OrderItem item = OrderItem.of(freshProduct, freshSku, orderQuantity);
        Order order = Order.createForMember(1L, List.of(item));
        order = orderAdminRepository.saveAndFlush(order);

        orderAdminService.cancel(order.getId(), "재고 부족", "OUT_OF_STOCK");

        Order cancelled = orderAdminRepository.findById(order.getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        entityManager.flush();  // ★ dirty 변경사항 DB에 반영
        entityManager.clear();  // ★ 그 다음 캐시 제거
        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        SKU updatedSku = updated.getSkus().get(0);
        assertThat(updatedSku.getStock()).isEqualTo(stockBefore + orderQuantity);
    }

    @Test
    @DisplayName("없는 orderId → 예외")
    void cancelNotFoundOrderThrowsException() {
        assertThatThrownBy(() ->
                orderAdminService.cancel(99999L, "사유", "CODE")
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이미 취소된 주문 → 예외")
    void cancelAlreadyCancelledOrderThrowsException() {
        Order order = createPendingOrder();
        orderAdminService.cancel(order.getId(), "사유", "CODE");

        assertThatThrownBy(() ->
                orderAdminService.cancel(order.getId(), "사유", "CODE")
        ).isInstanceOf(BusinessException.class);
    }
}