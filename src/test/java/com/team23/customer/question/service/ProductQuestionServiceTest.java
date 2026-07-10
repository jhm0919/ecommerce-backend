package com.team23.customer.question.service;

import com.team23.common.exception.BusinessException;
import com.team23.customer.member.domain.Member;
import com.team23.customer.member.repository.MemberRepository;
import com.team23.customer.category.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.question.domain.ProductQuestion;
import com.team23.customer.question.domain.QuestionStatus;
import com.team23.customer.question.dto.QuestionCreateRequest;
import com.team23.customer.question.exception.QuestionNotDeletableException;
import com.team23.customer.question.exception.QuestionNotFoundException;
import com.team23.customer.question.exception.QuestionNotOwnerException;
import com.team23.customer.question.repository.ProductQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductQuestionServiceTest {

    @Mock private ProductQuestionRepository questionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks private ProductQuestionService questionService;

    private Product testProduct;
    private Member testMember;
    private ProductQuestion testQuestion;

    @BeforeEach
    void setUp() {
        testProduct = Product.register(
                "티셔츠",
                BigDecimal.valueOf(29900),
                "설명",
                "https://image.url",
                Category.create("상의", "tops")
        );
        testMember = Member.registerFromOAuth(
                com.team23.customer.member.domain.AuthProvider.GOOGLE,
                "google-sub-001",
                "test@example.com",
                true,
                "홍길동",
                null,
                "ko"
        );
        testQuestion = ProductQuestion.create(
                1L, "티셔츠", 100L, "홍길동", "사이즈 문의드립니다.", false
        );
    }

    @Nested
    @DisplayName("질문 등록 (create)")
    class Create {

        @Test
        @DisplayName("정상 등록")
        void createNormal() {
            given(productRepository.findById(1L))
                    .willReturn(Optional.of(testProduct));
            given(memberRepository.findById(100L))
                    .willReturn(Optional.of(testMember));
            given(questionRepository.save(any(ProductQuestion.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            QuestionCreateRequest request =
                    new QuestionCreateRequest("사이즈 문의드립니다.", false);

            ProductQuestion result = questionService.create(1L, 100L, request);

            assertThat(result.getContent()).isEqualTo("사이즈 문의드립니다.");
            assertThat(result.getStatus()).isEqualTo(QuestionStatus.PENDING);
            assertThat(result.isSecret()).isFalse();
            verify(questionRepository).save(any(ProductQuestion.class));
        }

        @Test
        @DisplayName("비밀글 등록")
        void createSecret() {
            given(productRepository.findById(1L))
                    .willReturn(Optional.of(testProduct));
            given(memberRepository.findById(100L))
                    .willReturn(Optional.of(testMember));
            given(questionRepository.save(any(ProductQuestion.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            QuestionCreateRequest request =
                    new QuestionCreateRequest("비밀 질문입니다.", true);

            ProductQuestion result = questionService.create(1L, 100L, request);

            assertThat(result.isSecret()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.create(
                    999L, 100L, new QuestionCreateRequest("질문", false)))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(questionRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 예외")
        void rejectUnknownMember() {
            given(productRepository.findById(1L))
                    .willReturn(Optional.of(testProduct));
            given(memberRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.create(
                    1L, 999L, new QuestionCreateRequest("질문", false)))
                    .isInstanceOf(BusinessException.class);

            verify(questionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("상품별 질문 목록 조회 (findByProduct)")
    class FindByProduct {

        @Test
        @DisplayName("정상 조회")
        void findByProductNormal() {
            given(productRepository.findById(1L))
                    .willReturn(Optional.of(testProduct));
            given(questionRepository.findByProductIdOrderByCreatedAtDesc(
                    any(), any()))
                    .willReturn(new PageImpl<>(List.of(testQuestion)));

            var result = questionService.findByProduct(1L, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.findByProduct(
                    999L, PageRequest.of(0, 20)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("질문 삭제 (delete)")
    class Delete {

        @Test
        @DisplayName("본인 질문 + 답변 없음 → 삭제 가능")
        void deleteNormal() {
            given(questionRepository.findWithLockById(1L))
                    .willReturn(Optional.of(testQuestion));  // PENDING 상태 그대로

            assertThatCode(() -> questionService.delete(1L, 100L))
                    .doesNotThrowAnyException();

            verify(questionRepository).delete(testQuestion);
        }

        @Test
        @DisplayName("타인 질문 삭제 시도 → 예외")
        void rejectNotOwner() {
            given(questionRepository.findWithLockById(1L))
                    .willReturn(Optional.of(testQuestion));

            assertThatThrownBy(() -> questionService.delete(1L, 999L))
                    .isInstanceOf(QuestionNotOwnerException.class);

            verify(questionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("답변 있는 질문 삭제 시도 → 예외")
        void rejectAnsweredQuestion() {
            // ★ 공유 객체 변경 대신 로컬 인스턴스 생성
            ProductQuestion answeredQuestion = ProductQuestion.create(
                    1L, "티셔츠", 100L, "홍길동", "사이즈 문의드립니다.", false
            );
            answeredQuestion.answer("답변 내용");  // 로컬 객체만 변경

            given(questionRepository.findWithLockById(1L))
                    .willReturn(Optional.of(answeredQuestion));

            assertThatThrownBy(() -> questionService.delete(1L, 100L))
                    .isInstanceOf(QuestionNotDeletableException.class);

            verify(questionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 질문 → 예외")
        void rejectUnknownQuestion() {
            given(questionRepository.findWithLockById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.delete(999L, 100L))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }
}