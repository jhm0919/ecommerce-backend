package com.team23.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청 진입 시점에 traceId 발급 + 전체 요청 시간 측정.
 *
 * <p>Servlet Container 영역 (Spring 진입 전)에서 동작.
 * <p>MDC 에 traceId 를 저장하여 모든 후속 로그(Interceptor/Service/Hibernate)에 자동 포함.
 */
@Slf4j
@Component
public class TraceIdFilter extends OncePerRequestFilter { // 한 요청에 한 번만 실행 보장 (forward, include 시 중복 방지), 일반 Filter보다 안전

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        try { // 예외 발생해도 MDC 정리 + 종료 로그 보장, 누락 시 다음 요청에 traceId 오염 가능 (심각한 버그)
            MDC.put(TRACE_ID_KEY, traceId);

            log.info(">>> 요청 시작: {} {}", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            log.info("<<< 요청 종료: {} {} - {} - {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);

            MDC.clear(); // 스레드 풀 재사용 시 오염 방지
        }

    }
}
