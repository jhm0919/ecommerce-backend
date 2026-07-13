package com.team23.ai.chat.controller;

import com.team23.ai.chat.client.FastApiClient;
import com.team23.ai.chat.dto.FastApiChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiChatControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FastApiClient fastApiClient;    // FastAPI 실제 호출 차단

    @Test
    @DisplayName("POST /api/ai/chat - FastAPI 정상 → 200")
    void chatFastApiSuccess_returns200() throws Exception {
        // FastAPI 응답 Mock
        FastApiChatResponse mockResponse = new FastApiChatResponse(
                "session-1",
                "비 오는 날엔 바람막이를 추천드립니다.",
                List.of(new FastApiChatResponse.RecommendationDto(
                        10L, "라이트 바람막이",
                        new BigDecimal("59000"),
                        "http://image.url",
                        "최근 검색어 반영"
                )),
                List.of("예산은 어느 정도인가요?")
        );
        given(fastApiClient.chat(any())).willReturn(mockResponse);

        Map<String, Object> body = Map.of(
                "sessionId", "session-1",
                "message", "비 오는 날 옷 추천해줘"
        );

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.answer").isNotEmpty())
                .andExpect(jsonPath("$.data.recommendations").isArray())
                .andExpect(jsonPath("$.data.sessionId").value("session-1"));
    }

    @Test
    @DisplayName("POST /api/ai/chat - FastAPI 장애 → Fallback 200")
    void chatFastApiDown_returnsFallback() throws Exception {
        // FastAPI 장애 시뮬레이션
        given(fastApiClient.chat(any())).willThrow(new RuntimeException("FastAPI 연결 실패"));

        Map<String, Object> body = Map.of(
                "sessionId", "session-1",
                "message", "옷 추천해줘"
        );

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())   // Fallback 도 200
                .andExpect(jsonPath("$.data.answer")
                        .value("현재 AI 추천 서버 연결이 불안정해서 기본 추천을 보여드립니다."))
                .andExpect(jsonPath("$.data.recommendations").isArray());
    }

    @Test
    @DisplayName("sessionId 없으면 → 400")
    void chatNoSessionId_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "message", "옷 추천해줘"
        );

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("message 없으면 → 400")
    void chatNoMessage_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "sessionId", "session-1"
        );

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비로그인 유저도 호출 가능 → 200")
    void chatAnonymous_returns200() throws Exception {
        given(fastApiClient.chat(any())).willReturn(
                new FastApiChatResponse(
                        "session-anon", "기본 추천입니다.", List.of(), List.of()
                )
        );

        Map<String, Object> body = Map.of(
                "sessionId", "session-anon",
                "message", "옷 추천해줘"
        );

        mockMvc.perform(post("/api/ai/chat")   // 인증 없이 호출
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}