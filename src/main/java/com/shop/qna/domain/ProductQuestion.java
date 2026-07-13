package com.shop.qna.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_questions", indexes = {
        @Index(name = "idx_question_product", columnList = "product_id"),
        @Index(name = "idx_question_member", columnList = "member_id"),
        @Index(name = "idx_question_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProductQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;  // 스냅샷

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_name", nullable = false, length = 100)
    private String memberName;  // 스냅샷

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "is_secret", nullable = false)
    private boolean secret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(length = 2000)
    private String answer;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    public static ProductQuestion create(
            Long productId,
            String productName,
            Long memberId,
            String memberName,
            String content,
            boolean secret
    ) {
        ProductQuestion question = new ProductQuestion();
        question.productId = productId;
        question.productName = productName;
        question.memberId = memberId;
        question.memberName = memberName;
        question.content = content;
        question.secret = secret;
        question.status = QuestionStatus.PENDING;
        return question;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    /**
     * 답변 등록.
     */
    public void answer(String answerContent) {
        if (this.status != QuestionStatus.PENDING) {
            throw new IllegalStateException("이미 답변이 등록된 질문입니다");
        }
        validateAnswerContent(answerContent);
        this.answer = answerContent;
        this.status = QuestionStatus.ANSWERED;
        this.answeredAt = LocalDateTime.now();
    }

    /**
     * 답변 수정.
     */
    public void updateAnswer(String newAnswerContent) {
        if (this.status != QuestionStatus.ANSWERED) {
            throw new IllegalStateException("답변이 없는 질문입니다");
        }
        validateAnswerContent(newAnswerContent);
        this.answer = newAnswerContent;
    }

    /**
     * 답변 삭제.
     * 답변 삭제 시 상태 PENDING으로 복구.
     */
    public void deleteAnswer() {
        if (this.status != QuestionStatus.ANSWERED) {
            throw new IllegalStateException("답변이 없는 질문입니다");
        }
        this.answer = null;
        this.answeredAt = null;
        this.status = QuestionStatus.PENDING;
    }

    /**
     * 질문 삭제 가능 여부.
     * PENDING 상태 (답변 없음)일 때만 삭제 가능.
     */
    public boolean isDeletable() {
        return this.status == QuestionStatus.PENDING;
    }

    /**
     * 질문 조회 가능 여부.
     * 비밀글이면 작성자 또는 판매자만 조회 가능.
     */
    public boolean isVisibleTo(Long memberId) {
        if (!this.secret) return true;
        return this.memberId.equals(memberId);
    }

    private void validateAnswerContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("답변 내용은 비어 있을 수 없습니다");
        }
    }
}
