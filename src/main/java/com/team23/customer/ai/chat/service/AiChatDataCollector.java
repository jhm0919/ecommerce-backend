package com.team23.customer.ai.chat.service;

import com.team23.customer.ai.behavior.domain.UserBehaviorLog;
import com.team23.customer.ai.behavior.repository.UserBehaviorLogRepository;
import com.team23.customer.ai.chat.dto.FastApiChatRequest;
import com.team23.customer.cart.domain.Cart;
import com.team23.customer.cart.domain.CartItem;
import com.team23.customer.cart.repository.CartRepository;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI Chat 에 필요한 사용자 데이터 수집.
 *
 * <p>AiChatService 에서 분리하여 단일 책임 유지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatDataCollector {
    private static final int CANDIDATE_LIMIT = 20;
    private static final int ORDER_LIMIT = 5;

    private final UserBehaviorLogRepository behaviorLogRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public FastApiChatRequest collect(Long memberId, String sessionId, String message) {

        // 1. 행동 로그 (현재는 빈 리스트 — 추후 이슈에서 실제 수집)
        List<FastApiChatRequest.BehaviorLogDto> behaviorLogs = collectBehaviorLogs(memberId, sessionId);

        // 2. 장바구니
        List<FastApiChatRequest.CartItemDto> cartItems = collectCartItems(memberId);

        // 3. 최근 주문
        List<FastApiChatRequest.OrderDto> orders = collectRecentOrders(memberId);

        // 4. 후보 상품 (재고 있는 최신 20개)
        List<FastApiChatRequest.CandidateProductDto> candidateProducts = collectCandidateProducts();

        return new FastApiChatRequest(
                memberId,
                sessionId,
                message,
                List.of(),        // conversationHistory: FastAPI 가 관리
                behaviorLogs,
                cartItems,
                orders,
                candidateProducts
        );
    }

    // ─── 각 데이터 수집 ───────────────────────────────────────
    private List<FastApiChatRequest.BehaviorLogDto> collectBehaviorLogs(Long memberId, String sessionId) {
        List<UserBehaviorLog> logs;
        if (memberId == null) { // 회원일 때
            logs = behaviorLogRepository.findTop20ByMemberIdOrderByCreatedAtDesc(memberId);
        } else { // 비회원일 때
            logs = behaviorLogRepository.findTop20BySessionIdOrderByCreatedAtDesc(sessionId);
        }

        return logs.stream()
                .map(log -> new FastApiChatRequest.BehaviorLogDto(
                        log.getActionType().name(),
                        log.getProductId(),
                        log.getKeyword(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private List<FastApiChatRequest.CartItemDto> collectCartItems(Long memberId) {
        if (memberId == null) {
            return List.of();
        }

        try {
            Cart cart = cartRepository.findByMemberId(memberId)
                    .orElse(null);

            if (cart == null || cart.getItems().isEmpty()) {
                return List.of();
            }

            List<Long> productIds = cart.getItems().stream()
                    .map(CartItem::getProductId)
                    .distinct()
                    .toList();

            Map<Long, Product> productMap = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, p -> p));

            return cart.getItems().stream()
                    .map(item -> {
                        Product product = productMap.get(item.getProductId());
                        if (product == null) return null; // 상품 없으면 제외

                        return new FastApiChatRequest.CartItemDto(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                product.getPrice().getAmount(),
                                product.getCategory().getName()
                        );
                    })
                    .filter(Objects::nonNull) // null 제외
                    .toList();

        } catch (Exception e) {
            log.warn("[AI Chat] 장바구니 조회 실패 (무시): {}", e.getMessage());
            return List.of();
        }
    }

    private List<FastApiChatRequest.OrderDto> collectRecentOrders(Long memberId) {
        if (memberId == null) {
            return List.of();
        }

        try {
            return orderRepository
                    .findTop5ByMemberIdOrderByCreatedAtDesc(memberId)
                    .stream()
                    .map(order -> new FastApiChatRequest.OrderDto(
                            order.getId(),
                            order.getCreatedAt(),
                            order.getItems().stream()
                                    .map(item -> new FastApiChatRequest.OrderItemDto(
                                            item.getProductId(),
                                            item.getProductName(),
                                            item.getQuantity(),
                                            item.getPriceAtOrder().getAmount()
                                    ))
                                    .toList()
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("[AI Chat] 주문 조회 실패 (무시): {}", e.getMessage());
            return List.of();
        }
    }

    private List<FastApiChatRequest.CandidateProductDto> collectCandidateProducts() {
        try {
            return productRepository
                    .findTop20ByStockGreaterThanOrderByCreatedAtDesc(0)
                    .stream()
                    .map(p -> new FastApiChatRequest.CandidateProductDto(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getCategory().getName(),
                            p.getPrice().getAmount(),
                            p.getTotalSkuStock(),
                            p.getMainImageUrl()
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("[AI Chat] 후보 상품 조회 실패 (무시): {}", e.getMessage());
            return List.of();
        }
    }
}