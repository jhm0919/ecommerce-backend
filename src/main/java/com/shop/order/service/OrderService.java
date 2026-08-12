package com.shop.order.service;

import com.shop.cart.exception.ProductNotPurchasableException;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderDetailResponse;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.repository.OrderRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import io.micrometer.core.annotation.Timed;
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
    private final ProductRepository productRepository;

    // ─────────────────────────────────────
    // 주문 생성
    // ─────────────────────────────────────
    @Timed(
            value = "order.member.create.time",
            description = "회원 주문 생성 처리 시간"
    )
    @Transactional
    public OrderDetailResponse createOrder(
            Long memberId,
            CreateOrderRequest request
    ) {
        List<OrderItem> items = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest req : request.items()) {
            Product product = productRepository.findByIdWithPessimistic(req.productId())
                    .orElseThrow(() -> new ProductNotFoundException(req.productId()));

//            Product product = productRepository.findById(req.productId())
//                    .orElseThrow(() -> new ProductNotFoundException(req.productId()));

            if (product.isPurchasable()) { // 상품 상태가 ACTIVE이면 통과
                throw new ProductNotPurchasableException(product.getId());
            }

            Sku sku = product.findSkuById(req.skuId());

            product.decreaseSkuStock(req.skuId(), req.quantity());

            items.add(OrderItem.of(product, sku, req.quantity()));
        }

        Order order = Order.createOrder(
                memberId,
                items,
                request.zipCode(),
                request.address(),
                request.receiverName(),
                request.receiverPhone(),
                request.memo()
        );
        orderRepository.save(order);

        return OrderDetailResponse.from(order);
    }

    // ─────────────────────────────────────
    // 주문 조회 (변경 없음)
    // ─────────────────────────────────────

    @Timed(
            value = "order.member.search.time",
            description = "회원 주문 목록 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Page<Order> findOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable);
    }

    @Timed(
            value = "order.member.detail.time",
            description = "회원 주문 상세 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Order findOrder(Long memberId, Long orderId) {
        return orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    // ─────────────────────────────────────
    // 주문 취소
    // ─────────────────────────────────────

    @Timed(
            value = "order.member.cancel.time",
            description = "회원 주문 취소 처리 시간"
    )
    @Transactional
    public Order cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdWithSkus(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            product.increaseSkuStock(item.getSkuId(), item.getQuantity());
        }

        order.cancel();

        log.info("Order cancelled: orderId={}, memberId={}", orderId, memberId);
        return order;
    }
}
