package com.team23.qna.service;

import com.team23.member.domain.Member;
import com.team23.member.exception.MemberNotFoundException;
import com.team23.member.repository.MemberRepository;
import com.team23.product.domain.Product;
import com.team23.product.exception.ProductNotFoundException;
import com.team23.product.repository.ProductRepository;
import com.team23.qna.domain.ProductQuestion;
import com.team23.qna.dto.QuestionCreateRequest;
import com.team23.qna.exception.QuestionNotDeletableException;
import com.team23.qna.exception.QuestionNotFoundException;
import com.team23.qna.exception.QuestionNotOwnerException;
import com.team23.qna.repository.ProductQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQuestionService {

    private final ProductQuestionRepository questionRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    /**
     * 질문 등록.
     */
    @Transactional
    public ProductQuestion create(
            Long productId,
            Long memberId,
            QuestionCreateRequest request
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        ProductQuestion question = ProductQuestion.create(
                productId,
                product.getName(),
                memberId,
                member.getName(),
                request.content(),
                request.secret()
        );

        log.info("Question created: productId={}, memberId={}", productId, memberId);
        return questionRepository.save(question);
    }

    /**
     * 상품별 질문 목록 조회.
     * 비밀글은 작성자에게만 내용 공개.
     */
    @Transactional(readOnly = true)
    public Page<ProductQuestion> findByProduct(Long productId, Pageable pageable) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return questionRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    /**
     * 질문 삭제.
     * 본인 질문 + 답변 없을 때만 가능.
     */
    @Transactional
    public void delete(Long questionId, Long memberId) {
        ProductQuestion question = questionRepository.findWithLockById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        // 본인 확인
        if (!question.getMemberId().equals(memberId)) {
            throw new QuestionNotOwnerException(questionId, memberId);
        }

        // 답변 없을 때만 삭제 가능
        if (!question.isDeletable()) {
            throw new QuestionNotDeletableException(questionId);
        }

        questionRepository.delete(question);
        log.info("Question deleted: questionId={}, memberId={}", questionId, memberId);
    }
}