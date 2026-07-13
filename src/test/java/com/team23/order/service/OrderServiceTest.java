package com.team23.order.service;

import com.team23.order.delivery.domain.Delivery;
import com.team23.order.delivery.repository.DeliveryRepository;
import com.team23.order.domain.Order;
import com.team23.order.domain.OrderItem;
import com.team23.order.dto.CreateOrderRequest;
import com.team23.order.exception.InsufficientStockException;
import com.team23.order.exception.OrderAccessDeniedException;
import com.team23.order.exception.OrderNotFoundException;
import com.team23.order.repository.OrderRepository;
import com.team23.category.domain.Category;
import com.team23.order.service.OrderService;
import com.team23.product.domain.Product;
import com.team23.product.domain.Sku;
import com.team23.product.domain.SkuOption;
import com.team23.product.domain.StockChangedEvent;
import com.team23.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ApplicationEventPublisher eventPublisher;  // ★ 추가

    @InjectMocks private OrderService orderService;

    private Product product;
    private Sku sku;

    @BeforeEach
    void setUp() {
        Category category = Category.create("의류", "clothing");
        product = Product.register(
                "티셔츠", BigDecimal.valueOf(29900), "설명", "img", category
        );
        setId(product, 1L);
        sku = product.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setId(sku, 100L);
    }

    private CreateOrderRequest createRequest(boolean isGuest) {
        return new CreateOrderRequest(
                List.of(new CreateOrderRequest.OrderItemRequest(1L, 100L, 2)),
                new CreateOrderRequest.DeliveryInfoRequest(
                        "홍길동", "010-1234-5678",
                        "12345", "서울시 강남구", "101호", "문 앞에"
                ),
                isGuest ? "guest@example.com" : null,
                isGuest ? "010-9999-8888" : null
        );
    }

    @Nested
    @DisplayName("회원 주문 생성")
    class CreateMemberOrder {

        @Test
        @DisplayName("정상적으로 주문을 생성한다")
        void createNormal() {
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            Order result = orderService.createMemberOrder(10L, createRequest(false));

            assertThat(result.getMemberId()).isEqualTo(10L);
            assertThat(result.isMemberOrder()).isTrue();
            assertThat(sku.getStock()).isEqualTo(48);  // SKU 재고 50 - 2
            verify(orderRepository).save(any(Order.class));
            verify(deliveryRepository).save(any(Delivery.class));
            // ★ 재고 이력 이벤트 발행 검증
            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("재고 부족 시 InsufficientStockException")
        void rejectInsufficientStock() {
            Category category = Category.create("의류", "clothing");
            Product lowStockProduct = Product.register(
                    "한정상품", BigDecimal.valueOf(10000), "설명", "img", category
            );
            setId(lowStockProduct, 1L);
            Sku lowStockSku = lowStockProduct.addSku(
                    List.of(new SkuOption("색상", "검정")), 1
            );
            setId(lowStockSku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(lowStockProduct));

            CreateOrderRequest request = new CreateOrderRequest(
                    List.of(new CreateOrderRequest.OrderItemRequest(1L, 100L, 5)),
                    new CreateOrderRequest.DeliveryInfoRequest(
                            "홍", "010-1", "12345", "서울", "101", null
                    ),
                    null, null
            );

            assertThatThrownBy(() -> orderService.createMemberOrder(10L, request))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("존재하지 않는 SKU ID 요청 시 예외")
        void rejectUnknownSkuId() {
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            CreateOrderRequest request = new CreateOrderRequest(
                    List.of(new CreateOrderRequest.OrderItemRequest(1L, 999L, 1)),
                    new CreateOrderRequest.DeliveryInfoRequest(
                            "홍", "010-1", "12345", "서울", "101", null
                    ),
                    null, null
            );

            assertThatThrownBy(() -> orderService.createMemberOrder(10L, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("비회원 주문 생성")
    class CreateGuestOrder {

        @Test
        @DisplayName("정상적으로 비회원 주문 생성")
        void createNormal() {
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            Order result = orderService.createGuestOrder(createRequest(true));

            assertThat(result.isGuestOrder()).isTrue();
            assertThat(result.getGuestEmail()).isEqualTo("guest@example.com");
            assertThat(result.getGuestPhone()).isEqualTo("010-9999-8888");
            // ★ 재고 이력 이벤트 발행 검증
            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("guestEmail이 없으면 예외")
        void rejectMissingEmail() {
            CreateOrderRequest request = new CreateOrderRequest(
                    List.of(new CreateOrderRequest.OrderItemRequest(1L, 100L, 1)),
                    new CreateOrderRequest.DeliveryInfoRequest(
                            "홍", "010-1", "12345", "서울", "101", null
                    ),
                    null,
                    "010-9999-8888"
            );

            assertThatThrownBy(() -> orderService.createGuestOrder(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class FindOrders {

        @Test
        @DisplayName("자기 주문은 조회 가능")
        void findOwnOrder() {
            Order order = Order.createForMember(10L, List.of(
                    OrderItem.of(product, sku, 1)
            ));

            given(orderRepository.findByIdAndMemberIdWithItems(1L, 10L))
                    .willReturn(Optional.of(order));

            Order result = orderService.findMyOrder(10L, 1L);

            assertThat(result).isEqualTo(order);
        }

        @Test
        @DisplayName("다른 회원의 주문은 NotFound로 응답")
        void rejectOtherMembersOrder() {
            given(orderRepository.findByIdAndMemberIdWithItems(1L, 10L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findMyOrder(10L, 1L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("비회원 조회 보안")
    class GuestOrderAccess {

        @Test
        @DisplayName("주문번호 + 이메일 일치 시 조회 가능")
        void allowsAccessWithMatchingEmail() {
            Order guestOrder = Order.createForGuest(
                    "guest@example.com", "010-9999-8888",
                    List.of(OrderItem.of(product, sku, 1))
            );

            given(orderRepository.findByOrderNumberWithItems(any()))
                    .willReturn(Optional.of(guestOrder));

            Order result = orderService.findGuestOrder("ORD-...", "guest@example.com");

            assertThat(result).isEqualTo(guestOrder);
        }

        @Test
        @DisplayName("주문번호 + 전화번호 일치 시 조회 가능")
        void allowsAccessWithMatchingPhone() {
            Order guestOrder = Order.createForGuest(
                    "guest@example.com", "010-9999-8888",
                    List.of(OrderItem.of(product, sku, 1))
            );

            given(orderRepository.findByOrderNumberWithItems(any()))
                    .willReturn(Optional.of(guestOrder));

            Order result = orderService.findGuestOrder("ORD-...", "010-9999-8888");

            assertThat(result).isEqualTo(guestOrder);
        }

        @Test
        @DisplayName("연락처 불일치 시 AccessDenied")
        void rejectMismatchedContact() {
            Order guestOrder = Order.createForGuest(
                    "guest@example.com", "010-9999-8888",
                    List.of(OrderItem.of(product, sku, 1))
            );

            given(orderRepository.findByOrderNumberWithItems(any()))
                    .willReturn(Optional.of(guestOrder));

            assertThatThrownBy(() ->
                    orderService.findGuestOrder("ORD-...", "wrong@example.com"))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문번호는 NotFound")
        void rejectUnknownOrderNumber() {
            given(orderRepository.findByOrderNumberWithItems(any()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.findGuestOrder("UNKNOWN", "anything"))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("자기 주문 취소 시 SKU 재고 복구 + ORDER_CANCEL 이벤트 발행")
        void cancelRestoresSkuStock() {
            Order order = Order.createForMember(10L, List.of(
                    OrderItem.of(product, sku, 3)
            ));

            given(orderRepository.findByIdAndMemberIdWithItems(1L, 10L))
                    .willReturn(Optional.of(order));
            given(productRepository.findById(any())).willReturn(Optional.of(product));

            orderService.cancelMyOrder(10L, 1L);

            assertThat(sku.getStock()).isEqualTo(53);  // 50 + 3 복구
            assertThat(order.getStatus().toString()).isEqualTo("CANCELLED");
            // ★ ORDER_CANCEL 이벤트 발행 검증
            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }
    }

    // ─── 테스트 헬퍼 ───

    private static void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}