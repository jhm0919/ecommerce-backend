package com.team23.customer.ai.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "sessionId 는 필수입니다")
        String sessionId,

        @NotBlank(message = "message 는 필수입니다")
        String message
) {

}
