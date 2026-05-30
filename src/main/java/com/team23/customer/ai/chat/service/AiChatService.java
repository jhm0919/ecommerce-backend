package com.team23.customer.ai.chat.service;

import com.team23.customer.ai.behavior.repository.UserBehaviorLogRepository;
import com.team23.customer.ai.chat.client.FastApiClient;
import com.team23.customer.ai.chat.dto.AiChatResponse;
import com.team23.customer.ai.chat.dto.FastApiChatRequest;
import com.team23.customer.ai.chat.dto.FastApiChatResponse;
import com.team23.customer.cart.repository.CartRepository;
import com.team23.customer.order.repository.OrderRepository;
import com.team23.customer.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatService {
    private static final int CANDIDATE_PRODUCT_LIMIT = 20;
    private static final int FALLBACK_PRODUCT_LIMIT = 3;

    private final UserBehaviorLogRepository behaviorLogRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FastApiClient fastApiClient;
    private final AiChatDataCollector dataCollector;

    /**
     * AI 채팅 처리.
     *
     * <p>사용자 데이터 수집 → FastAPI 호출 → 응답 반환.
     * <p>FastAPI 장애 시 Fallback (최신 상품 3개).
     */
    public AiChatResponse chat(Long memberId, String sessionId, String message) {

        // 1. 데이터 수집
        FastApiChatRequest fastApiRequest = dataCollector.collect(
                memberId, sessionId, message
        );

        // 2. FastAPI 호출 (장애 시 Fallback)
        try {
            FastApiChatResponse fastApiResponse = fastApiClient.chat(fastApiRequest);
            return AiChatResponse.from(fastApiResponse);
        } catch (Exception e) {
            log.warn("[AI Chat] FastAPI 호출 실패, Fallback 적용: {}", e.getMessage());
            return buildFallback(sessionId);
        }
    }

    // ─── Fallback ────────────────────────────────────────────

    private AiChatResponse buildFallback(String sessionId) {
        List<AiChatResponse.RecommendationDto> latestProducts =
                productRepository.findTop3ByStockGreaterThanOrderByCreatedAtDesc(0)
                        .stream()
                        .map(p -> new AiChatResponse.RecommendationDto(
                                p.getId(),
                                p.getName(),
                                p.getPrice().getAmount(),
                                p.getMainImageUrl(),
                                "최신 상품"
                        ))
                        .toList();

        return AiChatResponse.fallback(sessionId, latestProducts);
    }
}
