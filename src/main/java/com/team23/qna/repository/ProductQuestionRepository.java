package com.team23.qna.repository;

import com.team23.qna.domain.ProductQuestion;
import com.team23.qna.domain.QuestionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ProductQuestionRepository extends JpaRepository<ProductQuestion, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductQuestion> findWithLockById(Long id);
    /**
     * 상품별 질문 목록 (최신순).
     */
    Page<ProductQuestion> findByProductIdOrderByCreatedAtDesc(
            Long productId, Pageable pageable);

    /**
     * 판매자용 — 상태 필터.
     */
    Page<ProductQuestion> findByStatusOrderByCreatedAtAsc(
            QuestionStatus status, Pageable pageable);
}