package com.team23.customer.order.service;

import com.team23.customer.ai.behavior.event.BehaviorLogEvent;
import com.team23.customer.delivery.domain.Address;
import com.team23.customer.delivery.domain.Delivery;
import com.team23.customer.delivery.domain.Receiver;
import com.team23.customer.delivery.repository.DeliveryRepository;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.dto.CreateOrderRequest;
import com.team23.customer.order.dto.OrderDetailResponse;
import com.team23.customer.order.exception.InsufficientStockException;
import com.team23.customer.order.exception.OrderAccessDeniedException;
import com.team23.customer.order.exception.OrderNotFoundException;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.domain.StockChangedEvent;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.stockhistory.domain.StockChangeType;
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
    private final ApplicationEventPublisher eventPublisher;

    // ─────────────────────────────────────
    // 주문 생성
    // ─────────────────────────────────────

    @Transactional
    public Order createMemberOrder(Long memberId, CreateOrderRequest request) {
        List<PreparedOrderItem> prepared = prepareItemsAndDecreaseStock(request.items());
        List<OrderItem> items = prepared.stream()
                .map(PreparedOrderItem::orderItem)
                .toList();

        Order order = Order.createForMember(memberId, items);
        orderRepository.save(order);

        // ★ 주문 저장 후 이벤트 발행 (orderId 확보)
        publishStockChangedEvents(prepared, order.getId(), StockChangeType.ORDER);

        createDelivery(order.getId(), request.delivery());

        log.info("Member order created: orderId={}, memberId={}, total={}",
                order.getId(), memberId, order.getTotalAmount());
        return order;
    }

    @Timed(
            value = "order.member.create.time",
            description = "회원 주문 생성 처리 시간"
    )
    @Transactional
    public OrderDetailResponse createMemberOrderDetail(
            Long memberId,
            String sessionId,  // ★ sessionId 추가
            CreateOrderRequest request

    ) {
        Order order = createMemberOrder(memberId, request);
        Delivery delivery = findDeliveryByOrderId(order.getId());

        // 행동 로그 이벤트 발행
        eventPublisher.publishEvent(BehaviorLogEvent.createOrder(this, memberId, sessionId, order.getOrderNumber()));

        return OrderDetailResponse.from(order, delivery);
    }

    @Transactional
    public Order createGuestOrder(CreateOrderRequest request) {
        validateGuestInfo(request);

        List<PreparedOrderItem> prepared = prepareItemsAndDecreaseStock(request.items());
        List<OrderItem> items = prepared.stream()
                .map(PreparedOrderItem::orderItem)
                .toList();

        Order order = Order.createForGuest(
                request.guestEmail(),
                request.guestPhone(),
                items
        );
        orderRepository.save(order);

        // ★ 주문 저장 후 이벤트 발행
        publishStockChangedEvents(prepared, order.getId(), StockChangeType.ORDER);

        createDelivery(order.getId(), request.delivery());

        log.info("Guest order created: orderId={}, email={}, total={}",
                order.getId(), request.guestEmail(), order.getTotalAmount());
        return order;
    }

    @Timed(
            value = "order.guest.create.time",
            description = "비회원 주문 생성 처리 시간"
    )
    @Transactional
    public OrderDetailResponse createGuestOrderDetail(CreateOrderRequest request) {
        Order order = createGuestOrder(request);
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

    @Timed(
            value = "order.guest.search.time",
            description = "비회원 주문 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Order findGuestOrder(String orderNumber, String contact) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        if (!order.matchesGuestContact(contact)) {
            log.warn("Guest order access denied: orderNumber={}", orderNumber);
            throw new OrderAccessDeniedException();
        }

        return order;
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

            SKU sku = product.findSkuById(item.getSkuId()).orElseThrow();
            int stockBefore = sku.getStock();

            product.increaseSkuStock(item.getSkuId(), item.getQuantity());

            // ★ ORDER_CANCEL 이벤트 발행
            eventPublisher.publishEvent(StockChangedEvent.of(
                    product, sku,
                    StockChangeType.ORDER_CANCEL,
                    item.getQuantity(),
                    stockBefore,
                    sku.getStock(),
                    orderId
            ));
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

            SKU sku = product.findSkuById(req.skuId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "SKU not found in product: productId=" + req.productId()
                                    + ", skuId=" + req.skuId()));

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

    /**
     * 주문 저장 후 재고 변동 이력 이벤트 발행.
     * orderId 확보 후 호출.
     */
    private void publishStockChangedEvents(
            List<PreparedOrderItem> prepared,
            Long orderId,
            StockChangeType changeType
    ) {
        for (PreparedOrderItem p : prepared) {
            eventPublisher.publishEvent(StockChangedEvent.of(
                    p.product(),
                    p.sku(),
                    changeType,
                    p.quantity(),
                    p.stockBefore(),
                    p.stockAfter(),
                    orderId
            ));
        }
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

    private void validateGuestInfo(CreateOrderRequest request) {
        if (request.guestEmail() == null || request.guestEmail().isBlank()) {
            throw new IllegalArgumentException("Guest email is required");
        }
        if (request.guestPhone() == null || request.guestPhone().isBlank()) {
            throw new IllegalArgumentException("Guest phone is required");
        }
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
            SKU sku,
            int stockBefore,
            int stockAfter,
            int quantity
    ) {}
}
