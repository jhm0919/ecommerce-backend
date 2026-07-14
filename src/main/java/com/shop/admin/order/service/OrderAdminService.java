package com.shop.admin.order.service;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderItem;
import com.shop.order.domain.OrderStatus;
import com.shop.admin.order.dto.OrderAdminCancelResponse;
import com.shop.admin.order.dto.OrderAdminConfirmResponse;
import com.shop.admin.order.dto.OrderAdminListResponse;
import com.shop.admin.order.repository.OrderAdminRepository;
import com.shop.product.domain.Product;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import com.shop.product.repository.SkuRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderAdminService {
    private final OrderAdminRepository orderAdminRepository;
    private final ProductRepository productRepository;

    @Timed(
            value = "order.seller.search.time",
            description = "판매자 주문 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Page<OrderAdminListResponse> search(
            OrderStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to != null) ? to.atTime(23, 59, 59) : null;

        return orderAdminRepository
                .search(status, fromDt, toDt, pageable)
                .map(OrderAdminListResponse::from);
    }

    @Timed(
            value = "order.seller.confirm.time",
            description = "판매자 주문 확정 처리 시간"
    )
    public OrderAdminConfirmResponse confirm(List<Long> orderIds) {

        List<Order> orders = orderIds.stream()
                .map(id -> orderAdminRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.ORDER_NOT_FOUND) {}))
                .toList();

        // 전체 검증 먼저 — 하나라도 실패 시 전체 롤백
        orders.forEach(order -> {
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {};
            }
        });

        // 전부 통과 시 일괄 확정
//        orders.forEach(OrderAdmin::confirm);
//        for (Order order : orders) {
//            order.confirm();
//        }
        orders.forEach(Order::confirm);

        List<Long> confirmedIds = orders.stream()
                .map(com.shop.order.domain.Order::getId)
                .toList();

        return new OrderAdminConfirmResponse(orders.size(), confirmedIds);
    }

    @Timed(
            value = "order.seller.cancel.time",
            description = "판매자 주문 강제 취소 처리 시간"
    )
    public OrderAdminCancelResponse cancel(
            Long orderId,
            String cancelReason,
            String cancelReasonCode
    ) {
        // 1. 주문 조회
        Order order = orderAdminRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ORDER_NOT_FOUND) {});

        // 2. 강제 취소
//        OrderAdmin.forceCancel(order);
        order.forceCancel();

        // 3. 재고 복구 — 팀원 패턴 그대로
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            product.increaseSkuStock(item.getSkuId(), item.getQuantity());
        }

        log.info("판매자 강제 취소: orderId={}, reason={}", orderId, cancelReason);

        return OrderAdminCancelResponse.from(order, cancelReason);
    }
}
