package com.shop.order.controller;

import com.shop.order.delivery.domain.Delivery;
import com.shop.global.response.CommonResponse;
import com.shop.order.domain.Order;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderDetailResponse;
import com.shop.order.dto.OrderResponse;
import com.shop.order.service.OrderService;
import com.shop.global.security.jwt.AuthPrincipal;
import io.micrometer.core.annotation.Counted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문", description = "회원/비회원 주문 API")
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ─────────────────────────────────────
    // 회원용 API
    // ─────────────────────────────────────

    @Operation(summary = "회원 주문 생성", description = "로그인한 회원의 주문을 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "재고 부족 / 입력값 오류"),
            @ApiResponse(responseCode = "404", description = "상품 또는 SKU 없음")
    })
    @Counted(
            value = "order.member.create",
            description = "회원 주문 생성 요청 수"
    )
    @PostMapping("/me")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> createMemberOrder(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(
                        orderService.createMemberOrderDetail(principal.memberId(), request)
                ));
    }

    @Operation(summary = "내 주문 목록", description = "로그인한 회원의 주문 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Counted(
            value = "order.member.search",
            description = "회원 주문 목록 조회 요청 수"
    )
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable
    ) {
        Page<Order> orders = orderService.findMyOrders(principal.memberId(), pageable);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(orders.map(OrderResponse::from))
        );
    }

    @Operation(summary = "내 주문 상세", description = "로그인한 회원의 주문 상세를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "주문 없음")
    })
    @Counted(
            value = "order.member.detail",
            description = "회원 주문 상세 조회 요청 수"
    )
    @GetMapping("/me/{orderId}")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> getMyOrderDetail(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long orderId
    ) {
        Order order = orderService.findMyOrder(principal.memberId(), orderId);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(OrderDetailResponse.from(order, delivery))
        );
    }

    @Operation(summary = "내 주문 취소", description = "PENDING 상태의 주문만 취소 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "취소 불가 상태"),
            @ApiResponse(responseCode = "404", description = "주문 없음")
    })
    @Counted(
            value = "order.member.cancel",
            description = "회원 주문 취소 요청 수"
    )
    @PostMapping("/me/{orderId}/cancel")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> cancelMyOrder(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long orderId
    ) {
        Order order = orderService.cancelMyOrder(principal.memberId(), orderId);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(OrderDetailResponse.from(order, delivery))
        );
    }

    // ─────────────────────────────────────
    // 비회원용 API
    // ─────────────────────────────────────

    @Operation(summary = "비회원 주문 생성", description = "비회원 주문을 생성한다. 이메일과 전화번호 필수.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "재고 부족 / 입력값 오류"),
            @ApiResponse(responseCode = "404", description = "상품 또는 SKU 없음")
    })
    @Counted(
            value = "order.guest.create",
            description = "비회원 주문 생성 요청 수"
    )
    @PostMapping("/guest")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> createGuestOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(
                        orderService.createGuestOrderDetail(request)
                ));
    }

    @Operation(summary = "비회원 주문 조회", description = "주문번호 + 연락처(이메일 또는 전화번호)로 조회.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "주문 없음 또는 연락처 불일치")
    })
    @Counted(
            value = "order.guest.search",
            description = "비회원 주문 조회 요청 수"
    )
    @GetMapping("/guest")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> getGuestOrder(
            @RequestParam String orderNumber,
            @RequestParam String contact
    ) {
        Order order = orderService.findGuestOrder(orderNumber, contact);
        Delivery delivery = orderService.findDeliveryByOrderId(order.getId());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(OrderDetailResponse.from(order, delivery))
        );
    }
}
