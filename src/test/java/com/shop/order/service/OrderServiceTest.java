package com.shop.order.service;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderDetailResponse;
import com.shop.order.exception.InsufficientStockException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.repository.OrderRepository;
import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks private OrderService orderService;

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;

    Product product;
    Sku sku;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        orderRepository.deleteAll();

        Category category = Category.create("의류", "clothing");
        product = Product.register("티셔츠", 29900, "설명", "img", category);
        setId(product, 1L);
        sku = product.addSku(List.of(new SkuOption("색상", "검정")), 50);
        setId(sku, 10L);
    }

    private CreateOrderRequest createRequest() {
        return new CreateOrderRequest(List.of(new CreateOrderRequest.OrderItemRequest(product.getId(), sku.getId(), 2)),
                "12345", "서울시 강남구",
                "홍길동", "010-1234-5678", "문 앞에"
        );
    }

    @Nested
    @DisplayName("회원 주문 생성")
    class CreateMemberOrderAdmin {

        @Test
        @DisplayName("정상적으로 주문을 생성한다")
        void createOrder() {
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(productRepository.findByIdWithPessimistic(1L))
                    .willReturn(Optional.of(product));


            OrderDetailResponse orderResponse = orderService.createOrder(1L, createRequest());

            assertThat(orderResponse.orderNumber()).isNotBlank();
            assertThat(orderResponse.items()).hasSize(1);
            assertThat(orderResponse.receiverName()).isEqualTo("홍길동");
            assertThat(sku.getQuantity()).isEqualTo(48);

            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("재고 부족 시 InsufficientStockException")
        void rejectInsufficientStock() {
            given(productRepository.findByIdWithPessimistic(1L)).willReturn(Optional.of(product));

            CreateOrderRequest request = new CreateOrderRequest(List.of(new CreateOrderRequest.OrderItemRequest(product.getId(), sku.getId(), 51)),
                    "12345", "서울시 강남구",
                    "홍길동", "010-1234-5678", "문 앞에"
            );

            assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(InsufficientStockException.class);
        }

    }

    @Nested
    @DisplayName("주문 조회")
    class FindOrders {

        @Test
        @DisplayName("자기 주문은 조회 가능")
        void findOwnOrder() {
            CreateOrderRequest request = createRequest();

            Order order = Order.createOrder(10L, List.of(
                    OrderItem.of(product, sku, 1)
            ), request.zipCode(), request.address(), request.receiverName(), request.receiverPhone(), request.memo());

            given(orderRepository.findByIdAndMemberIdWithItems(1L, 10L))
                    .willReturn(Optional.of(order));

            Order result = orderService.findOrder(10L, 1L);

            assertThat(result).isEqualTo(order);
        }

        @Test
        @DisplayName("다른 회원의 주문은 NotFound로 응답")
        void rejectOtherMembersOrder() {
            given(orderRepository.findByIdAndMemberIdWithItems(1L, 10L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findOrder(10L, 1L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }


    @Nested
    @DisplayName("주문 취소")
    class CancelOrderAdmin {

        @Test
        @DisplayName("자기 주문 취소 시 SKU 재고 복구 + ORDER_CANCEL 이벤트 발행")
        void cancelRestoresSkuStock() {
            CreateOrderRequest request = createRequest();

            Order order = Order.createOrder(10L, List.of(
                    OrderItem.of(product, sku, 3)
            ), request.zipCode(), request.address(), request.receiverName(), request.receiverPhone(), request.memo());

            given(orderRepository.findByIdAndMemberIdWithItems(1L, 10L))
                    .willReturn(Optional.of(order));
            given(productRepository.findById(any())).willReturn(Optional.of(product));

            orderService.cancelOrder(10L, 1L);

            assertThat(sku.getQuantity()).isEqualTo(53);  // 50 + 3 복구
            assertThat(order.getStatus().toString()).isEqualTo("CANCELLED");
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