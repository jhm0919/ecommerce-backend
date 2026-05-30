package com.team23.customer.ai.chat.dto;

import java.math.BigDecimal;
import java.util.List;

public record FastApiChatResponse(
        String sessionId,
        String answer,
        List<RecommendationDto> recommendations,
        List<String> followUpQuestions
) {
    public record RecommendationDto(
            Long productId,
            String name,
            BigDecimal price,
            String imageUrl,
            String reason
    ) {
    }
}
