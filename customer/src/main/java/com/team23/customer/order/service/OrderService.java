package com.team23.customer.order.service;

import com.team23.customer.delivery.domain.Address;
import com.team23.customer.delivery.domain.Delivery;
import com.team23.customer.delivery.domain.Receiver;
import com.team23.customer.delivery.repository.DeliveryRepository;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.domain.OrderItem;
import com.team23.customer.order.dto.CreateOrderRequest;
import com.team23.customer.order.exception.InsufficientStockException;
import com.team23.customer.order.exception.OrderAccessDeniedException;
import com.team23.customer.order.exception.OrderNotFoundException;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 회원 주문 생성.
     * Order와 Delivery를 함께 생성하고, 상품 재고를 차감한다.
     */
    @Transactional
    public Order createMemberOrder(Long memberId, CreateOrderRequest request) {
        List<OrderItem> items = prepareItemsAndDecreaseStock(request.items());

        Order order = Order.createForMember(memberId, items);
        orderRepository.save(order);

        createDelivery(order.getId(), request.delivery());

        log.info("Member order created: orderId={}, memberId={}, total={}",
                order.getId(), memberId, order.getTotalAmount());
        return order;
    }

    @Transactional
    public Order createGuestOrder(CreateOrderRequest request) {
        validateGuestInfo(request);

        List<OrderItem> items = prepareItemsAndDecreaseStock(request.items());

        Order order = Order.createForGuest(
                request.guestEmail(),
                request.guestPhone(),
                items
        );
        orderRepository.save(order);

        createDelivery(order.getId(), request.delivery());

        log.info("Guest order created: orderId={}, email={}, total={}",
                order.getId(), request.guestEmail(), order.getTotalAmount());
        return order;
    }

    // ─────────────────────────────────────
    // 주문 조회
    // ─────────────────────────────────────

    /**
     * 회원의 주문 목록 조회.
     */
    @Transactional(readOnly = true)
    public Page<Order> findMyOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable);
    }

    /**
     * 회원의 주문 상세 조회.
     * 다른 회원의 주문 ID로 시도해도 NotFound로 응답 (정보 누출 방지).
     */
    @Transactional(readOnly = true)
    public Order findMyOrder(Long memberId, Long orderId) {
        return orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * 비회원 주문 조회 (주문번호 + 연락처).
     * 둘 다 일치해야 응답. 그렇지 않으면 NotFound (보안).
     */
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

    /**
     * 주문에 대한 배송 정보 조회.
     * 호출자가 권한 검증을 마쳤다는 가정.
     */
    @Transactional(readOnly = true)
    public Delivery findDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Delivery not found for order: " + orderId));
    }

    // ─────────────────────────────────────
    // 주문 취소
    // ─────────────────────────────────────

    /**
     * 회원 주문 취소.
     * Order의 status를 CANCELLED로 변경, 재고 복구.
     *
     * <p>주의: 학습 단계에선 Delivery 상태 검증 생략.
     * 운영 환경에선 Delivery.status가 PREPARING일 때만 취소 가능해야 함.
     */
    @Transactional
    public Order cancelMyOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 재고 복구 — SKU 단위로
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            // ★ 변경: product.increaseStock → product.increaseSkuStock
            product.increaseSkuStock(item.getSkuId(), item.getQuantity());
        }

        order.cancel();

        log.info("Order cancelled: orderId={}, memberId={}", orderId, memberId);
        return order;
    }

    // ─── 헬퍼 (큰 변경!) ───

    /**
     * 요청에서 OrderItem 목록을 만들고 SKU 재고를 차감한다.
     * 재고 부족 시 예외 → 트랜잭션 롤백.
     */
    private List<OrderItem> prepareItemsAndDecreaseStock(
            List<CreateOrderRequest.OrderItemRequest> requests
    ) {
        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest req : requests) {
            // 1. Product 조회
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ProductNotFoundException(req.productId()));

            // 2. SKU 조회 (Product의 SKU여야 함 — 보안)
            SKU sku = product.findSkuById(req.skuId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "SKU not found in product: productId=" + req.productId() +
                                    ", skuId=" + req.skuId()));

            // 3. 재고 차감 (SKU 단위)
            try {
                product.decreaseSkuStock(req.skuId(), req.quantity());
            } catch (IllegalStateException e) {
                log.warn("Stock decrease failed: productId={}, skuId={}, quantity={}",
                        req.productId(), req.skuId(), req.quantity());
                throw new InsufficientStockException();
            }

            // 4. OrderItem 생성 (Product + SKU + quantity)
            items.add(OrderItem.of(product, sku, req.quantity()));
        }
        return items;
    }

    /**
     * Delivery를 생성하여 저장한다.
     */
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

    /**
     * 비회원 주문 정보 검증.
     */
    private void validateGuestInfo(CreateOrderRequest request) {
        if (request.guestEmail() == null || request.guestEmail().isBlank()) {
            throw new IllegalArgumentException("Guest email is required");
        }
        if (request.guestPhone() == null || request.guestPhone().isBlank()) {
            throw new IllegalArgumentException("Guest phone is required");
        }
    }
}
