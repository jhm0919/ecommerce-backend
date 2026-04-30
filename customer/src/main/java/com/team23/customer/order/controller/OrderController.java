package com.team23.customer.order.controller;

import com.team23.customer.delivery.domain.Delivery;
import com.team23.customer.order.domain.Order;
import com.team23.customer.order.dto.CreateOrderRequest;
import com.team23.customer.order.dto.OrderDetailResponse;
import com.team23.customer.order.dto.OrderResponse;
import com.team23.customer.order.service.OrderService;
import com.team23.customer.security.jwt.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ─────────────────────────────────────
    // 회원용 API
    // ─────────────────────────────────────

    /**
     * 회원 주문 생성.
     */
    @PostMapping("/me")
    public ResponseEntity<OrderDetailResponse> createMemberOrder(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        Order order = orderService.createMemberOrder(principal.memberId(), request);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());

        return ResponseEntity.status(201)
                .body(OrderDetailResponse.from(order, delivery));
    }

    /**
     * 내 주문 목록.
     */
    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable
    ) {
        Page<Order> orders = orderService.findMyOrders(principal.memberId(), pageable);
        return ResponseEntity.ok(orders.map(OrderResponse::from));
    }

    /**
     * 내 주문 상세.
     */
    @GetMapping("/me/{orderId}")
    public ResponseEntity<OrderDetailResponse> getMyOrderDetail(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long orderId
    ) {
        Order order = orderService.findMyOrder(principal.memberId(), orderId);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());
        return ResponseEntity.ok(OrderDetailResponse.from(order, delivery));
    }

    /**
     * 내 주문 취소.
     */
    @PostMapping("/me/{orderId}/cancel")
    public ResponseEntity<OrderDetailResponse> cancelMyOrder(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long orderId
    ) {
        Order order = orderService.cancelMyOrder(principal.memberId(), orderId);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());
        return ResponseEntity.ok(OrderDetailResponse.from(order, delivery));
    }

    // ─────────────────────────────────────
    // 비회원용 API
    // ─────────────────────────────────────

    /**
     * 비회원 주문 생성.
     */
    @PostMapping("/guest")
    public ResponseEntity<OrderDetailResponse> createGuestOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        Order order = orderService.createGuestOrder(request);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());

        return ResponseEntity.status(201)
                .body(OrderDetailResponse.from(order, delivery));
    }

    /**
     * 비회원 주문 조회 (주문번호 + 연락처).
     */
    @GetMapping("/guest")
    public ResponseEntity<OrderDetailResponse> getGuestOrder(
            @RequestParam String orderNumber,
            @RequestParam String contact
    ) {
        Order order = orderService.findGuestOrder(orderNumber, contact);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());
        return ResponseEntity.ok(OrderDetailResponse.from(order, delivery));
    }
}
