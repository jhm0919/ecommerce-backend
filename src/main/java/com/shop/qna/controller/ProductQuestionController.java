package com.shop.qna.controller;

import com.shop.global.response.CommonResponse;
import com.shop.global.security.jwt.AuthPrincipal;
import com.shop.qna.domain.ProductQuestion;
import com.shop.qna.dto.QuestionCreateRequest;
import com.shop.qna.dto.QuestionResponse;
import com.shop.qna.service.ProductQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "상품 Q&A", description = "상품 질문/답변 API")
@RestController
@RequiredArgsConstructor
public class ProductQuestionController {

    private final ProductQuestionService questionService;

    @Operation(summary = "질문 등록", description = "상품에 질문을 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/products/{productId}/questions")
    public ResponseEntity<CommonResponse<QuestionResponse>> create(
            @PathVariable Long productId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody QuestionCreateRequest request
    ) {
        ProductQuestion question = questionService.create(
                productId, principal.memberId(), request);

        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(
                        QuestionResponse.of(question, principal.memberId())
                ));
    }

    @Operation(summary = "상품 질문 목록", description = "상품의 질문 목록을 조회한다. 비밀글은 마스킹 처리.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/api/products/{productId}/questions")
    public ResponseEntity<CommonResponse<Page<QuestionResponse>>> findByProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable
    ) {
        Page<ProductQuestion> questions =
                questionService.findByProduct(productId, pageable);

        Long viewerId = principal != null ? principal.memberId() : null;

        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        questions.map(q -> QuestionResponse.of(q, viewerId))
                )
        );
    }

    @Operation(summary = "질문 삭제", description = "본인 질문 삭제. 답변이 없을 때만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "답변 존재"),
            @ApiResponse(responseCode = "403", description = "본인 질문 아님"),
            @ApiResponse(responseCode = "404", description = "질문 없음")
    })
    @DeleteMapping("/api/questions/{questionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        questionService.delete(questionId, principal.memberId());
        return ResponseEntity.noContent().build();
    }
}
