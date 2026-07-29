package com.shop.qna.service;

import com.shop.qna.domain.ProductQuestion;
import com.shop.qna.domain.QuestionStatus;
import com.shop.qna.dto.AnswerRequest;
import com.shop.qna.exception.QuestionNotFoundException;
import com.shop.qna.repository.ProductQuestionRepository;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductQuestionAdminServiceTest {

    @InjectMocks private ProductQuestionAdminService questionAdminService;

    @Mock private ProductQuestionRepository questionRepository;

    private ProductQuestion pendingQuestion;
    private ProductQuestion answeredQuestion;

    @BeforeEach
    void setUp() {
        pendingQuestion = ProductQuestion.create(
                1L, "티셔츠", 100L, "홍길동", "사이즈 문의드립니다.", false
        );
        answeredQuestion = ProductQuestion.create(
                1L, "티셔츠", 200L, "김철수", "색상 문의드립니다.", false
        );
        answeredQuestion.answer("검정색 재고 있습니다.");
    }

    @Nested
    @DisplayName("질문 목록 조회 (findQuestions)")
    class FindQuestions {

        @Test
        @DisplayName("전체 조회 (status 필터 없음)")
        void findAllQuestions() {
            given(questionRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(
                            List.of(pendingQuestion, answeredQuestion)));

            var result = questionAdminService.findQuestions(
                    null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("PENDING 필터 조회")
        void findPendingQuestions() {
            given(questionRepository.findByStatusOrderByCreatedAtAsc(
                    eq(QuestionStatus.PENDING), any()))
                    .willReturn(new PageImpl<>(List.of(pendingQuestion)));

            var result = questionAdminService.findQuestions(
                    QuestionStatus.PENDING, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus())
                    .isEqualTo(QuestionStatus.PENDING);
        }

        @Test
        @DisplayName("ANSWERED 필터 조회")
        void findAnsweredQuestions() {
            given(questionRepository.findByStatusOrderByCreatedAtAsc(
                    eq(QuestionStatus.ANSWERED), any()))
                    .willReturn(new PageImpl<>(List.of(answeredQuestion)));

            var result = questionAdminService.findQuestions(
                    QuestionStatus.ANSWERED, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus())
                    .isEqualTo(QuestionStatus.ANSWERED);
        }
    }

    @Nested
    @DisplayName("답변 등록 (answer)")
    class Answer {

        @Test
        @DisplayName("정상 답변 등록")
        void answerNormal() {
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(pendingQuestion));

            ProductQuestion result = questionAdminService.answer(
                    1L, new AnswerRequest("XL 사이즈 추천드립니다."));

            assertThat(result.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
            assertThat(result.getAnswer()).isEqualTo("XL 사이즈 추천드립니다.");
            assertThat(result.getAnsweredAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 답변된 질문 재답변 시도 → 예외")  // ★ 추가
        void rejectDuplicateAnswer() {
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(answeredQuestion));  // 이미 ANSWERED

            assertThatThrownBy(() -> questionAdminService.answer(
                    1L, new AnswerRequest("중복 답변")))
                    .isInstanceOf(IllegalStateException.class);

            // 기존 답변 유지 확인
            assertThat(answeredQuestion.getAnswer()).isEqualTo("검정색 재고 있습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 질문 → 예외")
        void rejectUnknownQuestion() {
            given(questionRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionAdminService.answer(
                    999L, new AnswerRequest("답변")))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("답변 수정 (updateAnswer)")
    class UpdateAnswer {

        @Test
        @DisplayName("정상 수정")
        void updateAnswerNormal() {
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(answeredQuestion));

            ProductQuestion result = questionAdminService.updateAnswer(
                    1L, new AnswerRequest("수정된 답변입니다."));

            assertThat(result.getAnswer()).isEqualTo("수정된 답변입니다.");
            assertThat(result.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        }

        @Test
        @DisplayName("답변 없는 질문 수정 시도 → 예외")
        void rejectUpdateWithoutAnswer() {
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(pendingQuestion));  // 답변 없음

            assertThatThrownBy(() -> questionAdminService.updateAnswer(
                    1L, new AnswerRequest("수정 내용")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("존재하지 않는 질문 → 예외")
        void rejectUnknownQuestion() {
            given(questionRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionAdminService.updateAnswer(
                    999L, new AnswerRequest("수정")))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("답변 삭제 (deleteAnswer)")
    class DeleteAnswer {

        @Test
        @DisplayName("정상 삭제 → PENDING 복구")
        void deleteAnswerNormal() {
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(answeredQuestion));

            assertThatCode(() -> questionAdminService.deleteAnswer(1L))
                    .doesNotThrowAnyException();

            assertThat(answeredQuestion.getStatus())
                    .isEqualTo(QuestionStatus.PENDING);
            assertThat(answeredQuestion.getAnswer()).isNull();
        }

        @Test
        @DisplayName("답변 없는 질문 삭제 시도 → 예외")
        void rejectDeleteWithoutAnswer() {
            given(questionRepository.findById(1L))
                    .willReturn(Optional.of(pendingQuestion));  // 답변 없음

            assertThatThrownBy(() -> questionAdminService.deleteAnswer(1L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("존재하지 않는 질문 → 예외")
        void rejectUnknownQuestion() {
            given(questionRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> questionAdminService.deleteAnswer(999L))
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }
}