// AnswerRequest.java
package com.team23.qna.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(
        @NotBlank(message = "답변 내용을 입력해주세요")
        @Size(max = 2000, message = "답변은 2000자 이하로 입력해주세요")
        String content
) {}
