package com.team23.customer.ai.chat.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiChatResponse(
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

    /** FastAPI 정상 응답 → 프론트 응답 변환 */
    public static AiChatResponse from(FastApiChatResponse response) {
        List<RecommendationDto> recs = response.recommendations().stream()
                .map(r -> new RecommendationDto(
                        r.productId(), r.name(), r.price(), r.imageUrl(), r.reason()))
                .toList();

        return new AiChatResponse(
                response.sessionId(),
                response.answer(),
                recs,
                response.followUpQuestions()
        );
    }

    /** Fallback 응답 생성 */
    public static AiChatResponse fallback(
            String sessionId,
            List<RecommendationDto> latestProducts
    ) {
        return new AiChatResponse(
                sessionId,
                "현재 AI 추천 서버 연결이 불안정해서 기본 추천을 보여드립니다.",
                latestProducts,
                List.of()
        );
    }
}
