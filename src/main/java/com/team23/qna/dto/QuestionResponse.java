package com.team23.qna.dto;

import com.team23.qna.domain.ProductQuestion;
import com.team23.qna.domain.QuestionStatus;

import java.time.LocalDateTime;

public record QuestionResponse(
        Long id,
        Long productId,
        String productName,
        String memberName,
        String content,
        boolean secret,
        QuestionStatus status,
        String answer,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {
    private static final String SECRET_CONTENT = "비밀 질문입니다.";
    private static final String SECRET_MEMBER = "비밀";

    /**
     * 소비자용 — 비밀글 마스킹 처리.
     */
    public static QuestionResponse of(ProductQuestion question, Long viewerMemberId) {
        boolean canView = question.isVisibleTo(viewerMemberId);

        return new QuestionResponse(
                question.getId(),
                question.getProductId(),
                question.getProductName(),
                canView ? question.getMemberName() : SECRET_MEMBER,
                canView ? question.getContent() : SECRET_CONTENT,
                question.isSecret(),
                question.getStatus(),
                canView ? question.getAnswer() : null,
                canView ? question.getAnsweredAt() : null,
                question.getCreatedAt()
        );
    }

    /**
     * 판매자용 — 마스킹 없이 전체 공개.
     */
    public static QuestionResponse forSeller(ProductQuestion question) {
        return new QuestionResponse(
                question.getId(),
                question.getProductId(),
                question.getProductName(),
                question.getMemberName(),
                question.getContent(),
                question.isSecret(),
                question.getStatus(),
                question.getAnswer(),
                question.getAnsweredAt(),
                question.getCreatedAt()
        );
    }
}
