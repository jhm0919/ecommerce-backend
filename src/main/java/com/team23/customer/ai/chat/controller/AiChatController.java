package com.team23.customer.ai.chat.controller;

import com.team23.common.response.CommonResponse;
import com.team23.common.security.jwt.AuthPrincipal;
import com.team23.customer.ai.chat.dto.AiChatRequest;
import com.team23.customer.ai.chat.dto.AiChatResponse;
import com.team23.customer.ai.chat.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 채팅", description = "AI 상품 추천 채팅 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "AI 추천 채팅",
            description = "사용자 메시지와 컨텍스트 데이터를 FastAPI 추천 서버로 전달한다. " +
                    "FastAPI 장애 시 최신 상품 3개를 Fallback 으로 반환한다.")
    @PostMapping("/chat")
    public ResponseEntity<CommonResponse<AiChatResponse>> chat(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        Long memberId = (principal != null) ? principal.memberId() : null;

        AiChatResponse response = aiChatService.chat(memberId, request.sessionId(), request.message());

        return ResponseEntity.ok(CommonResponse.createSuccess(response));
    }
}
