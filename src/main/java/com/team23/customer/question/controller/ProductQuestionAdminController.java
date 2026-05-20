package com.team23.customer.question.controller;

import com.team23.common.response.CommonResponse;
import com.team23.customer.question.domain.ProductQuestion;
import com.team23.customer.question.domain.QuestionStatus;
import com.team23.customer.question.dto.AnswerRequest;
import com.team23.customer.question.dto.QuestionResponse;
import com.team23.customer.question.service.ProductQuestionAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "판매자 - Q&A 관리", description = "판매자 질문 답변 관리 API")
@RestController
@RequestMapping("/api/seller/questions")
@RequiredArgsConstructor
public class ProductQuestionAdminController {

    private final ProductQuestionAdminService questionAdminService;

    @Operation(summary = "질문 목록 조회", description = "전체 질문 목록. status 필터 선택적.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<QuestionResponse>>> findQuestions(
            @RequestParam(required = false) QuestionStatus status,
            Pageable pageable
    ) {
        Page<ProductQuestion> questions =
                questionAdminService.findQuestions(status, pageable);

        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        questions.map(QuestionResponse::forSeller)
                )
        );
    }

    @Operation(summary = "답변 등록", description = "질문에 답변을 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "질문 없음")
    })
    @PostMapping("/{questionId}/answer")
    public ResponseEntity<CommonResponse<QuestionResponse>> answer(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        ProductQuestion question = questionAdminService.answer(questionId, request);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(QuestionResponse.forSeller(question))
        );
    }

    @Operation(summary = "답변 수정", description = "등록된 답변을 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "답변 없음"),
            @ApiResponse(responseCode = "404", description = "질문 없음")
    })
    @PatchMapping("/{questionId}/answer")
    public ResponseEntity<CommonResponse<QuestionResponse>> updateAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        ProductQuestion question =
                questionAdminService.updateAnswer(questionId, request);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(QuestionResponse.forSeller(question))
        );
    }

    @Operation(summary = "답변 삭제", description = "등록된 답변을 삭제한다. 질문 상태 PENDING으로 복구.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "답변 없음"),
            @ApiResponse(responseCode = "404", description = "질문 없음")
    })
    @DeleteMapping("/{questionId}/answer")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long questionId) {
        questionAdminService.deleteAnswer(questionId);
        return ResponseEntity.noContent().build();
    }
}
