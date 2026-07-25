package com.shop.order.service;

import com.shop.order.delivery.domain.Address;
import com.shop.order.delivery.domain.Delivery;
import com.shop.order.delivery.domain.Receiver;
import com.shop.order.delivery.repository.DeliveryRepository;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderDetailResponse;
import com.shop.order.exception.InsufficientStockException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.repository.OrderRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import com.shop.admin.stock.domain.StockType;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;

    // ─────────────────────────────────────
    // 주문 생성
    // ─────────────────────────────────────
    @Timed(
            value = "order.member.create.time",
            description = "회원 주문 생성 처리 시간"
    )
    @Transactional
    public OrderDetailResponse createMemberOrder(
            Long memberId,
            CreateOrderRequest request
    ) {
        // 재고 차감이 끝난 주문 상품과 이후 이벤트 발행에 필요한 스냅샷을 임시로 담아두는 객체
        List<PreparedOrderItem> prepared = prepareItemsAndDecreaseStock(request.items());

        List<OrderItem> items = prepared.stream()
                .map(PreparedOrderItem::orderItem)
                .toList();

        Order order = Order.createForMember(memberId, items);
        orderRepository.save(order);

        createDelivery(order.getId(), request.delivery());

        Delivery delivery = findDeliveryByOrderId(order.getId());

        return OrderDetailResponse.from(order, delivery);
    }

    // ─────────────────────────────────────
    // 주문 조회 (변경 없음)
    // ─────────────────────────────────────

    @Timed(
            value = "order.member.search.time",
            description = "회원 주문 목록 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Page<Order> findMyOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable);
    }

    @Timed(
            value = "order.member.detail.time",
            description = "회원 주문 상세 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Order findMyOrder(Long memberId, Long orderId) {
        return orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public Delivery findDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Delivery not found for order: " + orderId));
    }

    // ─────────────────────────────────────
    // 주문 취소
    // ─────────────────────────────────────

    @Timed(
            value = "order.member.cancel.time",
            description = "회원 주문 취소 처리 시간"
    )
    @Transactional
    public Order cancelMyOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            Sku sku = product.findSkuById(item.getSkuId()).orElseThrow();
            int stockBefore = sku.getStock();

            product.increaseSkuStock(item.getSkuId(), item.getQuantity());

//            // ★ ORDER_CANCEL 이벤트 발행
//            eventPublisher.publishEvent(StockChangedEvent.of(
//                    product, sku,
//                    StockType.ORDER_CANCEL,
//                    item.getQuantity(),
//                    stockBefore,
//                    sku.getStock(),
//                    orderId
//            ));
        }

        order.cancel();

        log.info("Order cancelled: orderId={}, memberId={}", orderId, memberId);
        return order;
    }

    // ─────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────

    /**
     * 재고 차감 + 변동 정보 수집.
     * orderId가 아직 없어서 이벤트 발행은 하지 않음.
     */
    private List<PreparedOrderItem> prepareItemsAndDecreaseStock(
            List<CreateOrderRequest.OrderItemRequest> requests
    ) {
        List<PreparedOrderItem> prepared = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest req : requests) {
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ProductNotFoundException(req.productId()));

            Sku sku = product.findSkuById(req.skuId())
                    .orElseThrow(() -> new OrderNotFoundException(req.skuId()));

            int stockBefore = sku.getStock();  // ★ 차감 전 재고 기록

            try {
                product.decreaseSkuStock(req.skuId(), req.quantity());
            } catch (IllegalStateException e) {
                log.warn("Stock decrease failed: productId={}, skuId={}, quantity={}",
                        req.productId(), req.skuId(), req.quantity());
                throw new InsufficientStockException();
            }

            prepared.add(new PreparedOrderItem(
                    OrderItem.of(product, sku, req.quantity()),
                    product,
                    sku,
                    stockBefore,
                    sku.getStock(),  // stockAfter
                    req.quantity()
            ));
        }

        return prepared;
    }

    private void createDelivery(Long orderId, CreateOrderRequest.DeliveryInfoRequest request) {
        Address address = new Address(
                request.zipCode(),
                request.addressLine1(),
                request.addressLine2()
        );
        Receiver receiver = new Receiver(
                request.receiverName(),
                request.receiverPhone()
        );

        Delivery delivery = Delivery.prepare(orderId, address, receiver, request.memo());
        deliveryRepository.save(delivery);
    }

    // ─────────────────────────────────────
    // 내부 record
    // ─────────────────────────────────────

    /**
     * 재고 차감 정보를 OrderItem과 함께 보관.
     * 주문 저장 후 이벤트 발행에 사용.
     */
    private record PreparedOrderItem(
            OrderItem orderItem,
            Product product,
            Sku sku,
            int stockBefore,
            int stockAfter,
            int quantity
    ) {}
}
