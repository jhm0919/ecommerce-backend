package com.team23.ai.chat.client;

import com.team23.ai.chat.dto.FastApiChatRequest;
import com.team23.ai.chat.dto.FastApiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * FastAPI 추천 서버 WebClient.
 *
 * <p>동기 방식 (.block()) — 현재 프로젝트가 Spring MVC 라 비동기 불필요.
 * <p>타임아웃 초과 또는 예외 발생 시 상위 Service 에서 Fallback 처리.
 */
@Slf4j
@Component
public class FastApiClient {
    private final WebClient webClient;
    private final Duration timeout;

    public FastApiClient(
            @Value("${app.fastapi.base-url}") String baseUrl,
            @Value("${app.fastapi.timeout-seconds:5}") long timeoutSeconds
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    /**
     * FastAPI POST /recommend/chat 호출.
     *
     * @throws Exception FastAPI 장애 / 타임아웃 시 (상위에서 Fallback)
     */
    public FastApiChatResponse chat(FastApiChatRequest request) {
        log.info("[FastAPI] 호출 시작: memberId={}, sessionId={}",
                request.memberId(), request.sessionId());

        FastApiChatResponse response = webClient.post()
                .uri("/recommend/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FastApiChatResponse.class)
                .timeout(timeout)
                .block();

        log.info("[FastAPI] 호출 성공: sessionId={}", request.sessionId());
        return response;
    }
}
