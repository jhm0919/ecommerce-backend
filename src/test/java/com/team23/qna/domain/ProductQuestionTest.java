package com.team23.qna.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductQuestionTest {

    private ProductQuestion question;
    private ProductQuestion secretQuestion;

    @BeforeEach
    void setUp() {
        question = ProductQuestion.create(
                1L, "티셔츠", 100L, "홍길동", "사이즈 문의드립니다.", false
        );
        secretQuestion = ProductQuestion.create(
                1L, "티셔츠", 100L, "홍길동", "비밀 질문입니다.", true
        );
    }

    @Nested
    @DisplayName("질문 생성")
    class Create {

        @Test
        @DisplayName("생성 시 기본 상태는 PENDING")
        void defaultStatusIsPending() {
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
        }

        @Test
        @DisplayName("생성 시 답변은 null")
        void defaultAnswerIsNull() {
            assertThat(question.getAnswer()).isNull();
            assertThat(question.getAnsweredAt()).isNull();
        }

        @Test
        @DisplayName("비밀글 설정")
        void secretFlag() {
            assertThat(question.isSecret()).isFalse();
            assertThat(secretQuestion.isSecret()).isTrue();
        }
    }

    @Nested
    @DisplayName("답변 등록")
    class Answer {

        @Test
        @DisplayName("답변 등록 시 ANSWERED 상태로 변경")
        void answerChangesStatusToAnswered() {
            question.answer("XL 사이즈 추천드립니다.");

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
            assertThat(question.getAnswer()).isEqualTo("XL 사이즈 추천드립니다.");
            assertThat(question.getAnsweredAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 답변된 질문에 재답변 시도 → 예외")
        void rejectAlreadyAnswered() {
            question.answer("첫 번째 답변");

            assertThatThrownBy(() -> question.answer("두 번째 답변"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 답변이 등록된");

            assertThat(question.getAnswer()).isEqualTo("첫 번째 답변");
        }

        @Test
        @DisplayName("null 답변 → 예외, 필드 오염 없음")
        void rejectNullAnswer() {
            assertThatThrownBy(() -> question.answer(null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
            assertThat(question.getAnswer()).isNull();       // ★
            assertThat(question.getAnsweredAt()).isNull();   // ★
        }

        @Test
        @DisplayName("빈 문자열 답변 → 예외, 필드 오염 없음")
        void rejectEmptyAnswer() {
            assertThatThrownBy(() -> question.answer(""))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
            assertThat(question.getAnswer()).isNull();       // ★
            assertThat(question.getAnsweredAt()).isNull();   // ★
        }

        @Test
        @DisplayName("공백 문자열 답변 → 예외, 필드 오염 없음")
        void rejectBlankAnswer() {
            assertThatThrownBy(() -> question.answer("   "))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
            assertThat(question.getAnswer()).isNull();       // ★
            assertThat(question.getAnsweredAt()).isNull();   // ★
        }
    }

    @Nested
    @DisplayName("답변 수정")
    class UpdateAnswer {

        @Test
        @DisplayName("ANSWERED 상태에서 수정 가능")
        void updateAnswerNormal() {
            question.answer("초기 답변");
            question.updateAnswer("수정된 답변");

            assertThat(question.getAnswer()).isEqualTo("수정된 답변");
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        }

        @Test
        @DisplayName("답변 없는 질문 수정 시도 → 예외")
        void rejectUpdateWithoutAnswer() {
            assertThatThrownBy(() -> question.updateAnswer("수정 내용"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("답변이 없는");
        }

        @Test
        @DisplayName("null로 수정 시도 → 예외, 기존 상태 유지")
        void rejectNullUpdate() {
            question.answer("초기 답변");

            assertThatThrownBy(() -> question.updateAnswer(null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(question.getAnswer()).isEqualTo("초기 답변");
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);  // ★
        }

        @Test
        @DisplayName("빈 문자열로 수정 시도 → 예외, 기존 상태 유지")
        void rejectEmptyUpdate() {
            question.answer("초기 답변");

            assertThatThrownBy(() -> question.updateAnswer(""))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(question.getAnswer()).isEqualTo("초기 답변");
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);  // ★
        }

        @Test
        @DisplayName("공백 문자열로 수정 시도 → 예외, 기존 상태 유지")
        void rejectBlankUpdate() {
            question.answer("초기 답변");

            assertThatThrownBy(() -> question.updateAnswer("   "))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(question.getAnswer()).isEqualTo("초기 답변");
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);  // ★
        }
    }

    @Nested
    @DisplayName("질문 삭제 가능 여부")
    class IsDeletable {

        @Test
        @DisplayName("PENDING 상태 → 삭제 가능")
        void pendingIsDeletable() {
            assertThat(question.isDeletable()).isTrue();
        }

        @Test
        @DisplayName("ANSWERED 상태 → 삭제 불가")
        void answeredIsNotDeletable() {
            question.answer("답변");
            assertThat(question.isDeletable()).isFalse();
        }
    }

    @Nested
    @DisplayName("조회 가능 여부 (비밀글)")
    class IsVisibleTo {

        @Test
        @DisplayName("공개 질문 → 누구나 조회 가능")
        void publicQuestionVisibleToAll() {
            assertThat(question.isVisibleTo(100L)).isTrue();
            assertThat(question.isVisibleTo(999L)).isTrue();
            assertThat(question.isVisibleTo(null)).isTrue();
        }

        @Test
        @DisplayName("비밀 질문 → 작성자만 조회 가능")
        void secretQuestionVisibleToOwnerOnly() {
            assertThat(secretQuestion.isVisibleTo(100L)).isTrue();   // 작성자
            assertThat(secretQuestion.isVisibleTo(999L)).isFalse();  // 타인
        }

        @Test
        @DisplayName("비밀 질문 → null(비로그인)은 조회 불가")
        void secretQuestionNotVisibleToGuest() {
            assertThat(secretQuestion.isVisibleTo(null)).isFalse();
        }
    }
}