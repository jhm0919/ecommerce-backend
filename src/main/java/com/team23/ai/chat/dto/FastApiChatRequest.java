package com.team23.ai.chat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FastApiChatRequest(
        Long memberId,
        String sessionId,
        String message,
        List<Object> conversationHistory,   // 항상 빈 배열 (FastAPI 가 관리)
        List<BehaviorLogDto> behaviorLogs,
        List<CartItemDto> cartItems,
        List<OrderDto> orders,
        List<CandidateProductDto> candidateProducts
) {

    // ─── 중첩 DTO ──────────────────────────────────────

    public record BehaviorLogDto(
            String actionType,
            Long productId,
            String keyword,
            LocalDateTime createdAt
    ) {
    }

    public record CartItemDto(
            Long productId,
            String name,
            int quantity,
            BigDecimal price,
            String categoryName
    ) {
    }

    public record OrderDto(
            Long orderId,
            LocalDateTime orderedAt,
            List<OrderItemDto> items
    ) {
    }

    public record OrderItemDto(
            Long productId,
            String name,
            int quantity,
            BigDecimal price
    ) {
    }

    public record CandidateProductDto(
            Long productId,
            String name,
            String description,
            String categoryName,
            BigDecimal price,
            int stock,
            String imageUrl
    ) {
    }
}
