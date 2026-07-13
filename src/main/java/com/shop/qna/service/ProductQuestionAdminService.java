package com.shop.qna.service;

import com.shop.qna.domain.ProductQuestion;
import com.shop.qna.domain.QuestionStatus;
import com.shop.qna.dto.AnswerRequest;
import com.shop.qna.exception.QuestionNotFoundException;
import com.shop.qna.repository.ProductQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQuestionAdminService {

    private final ProductQuestionRepository questionRepository;

    /**
     * 질문 목록 조회.
     * status 필터 선택적.
     */
    @Transactional(readOnly = true)
    public Page<ProductQuestion> findQuestions(QuestionStatus status, Pageable pageable) {
        if (status != null) {
            return questionRepository.findByStatusOrderByCreatedAtAsc(status, pageable);
        }
        return questionRepository.findAll(pageable);
    }

    /**
     * 답변 등록.
     */
    @Transactional
    public ProductQuestion answer(Long questionId, AnswerRequest request) {
        ProductQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        question.answer(request.content());

        log.info("Question answered: questionId={}", questionId);
        return question;
    }

    /**
     * 답변 수정.
     */
    @Transactional
    public ProductQuestion updateAnswer(Long questionId, AnswerRequest request) {
        ProductQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        question.updateAnswer(request.content());

        log.info("Answer updated: questionId={}", questionId);
        return question;
    }

    /**
     * 답변 삭제.
     * 상태 PENDING으로 복구.
     */
    @Transactional
    public void deleteAnswer(Long questionId) {
        ProductQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        question.deleteAnswer();

        log.info("Answer deleted: questionId={}", questionId);
    }
}
