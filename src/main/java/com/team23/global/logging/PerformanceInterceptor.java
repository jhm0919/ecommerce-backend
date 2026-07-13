package com.team23.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring 영역 진입~종료 시간 측정.
 *
 * <p>Filter 와의 시간 차이로 서블릿/디스패처 처리 시간 추정 가능.
 */
@Slf4j
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final String START_TIME_KEY = "interceptorStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_KEY, System.currentTimeMillis()); // request.setAttribute()로 시간 전달

        if (handler instanceof HandlerMethod handlerMethod) { // @Controller 메서드만 HandlerMethod
            log.info("[Interceptor] Spring 진입: {}.{}",
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_KEY);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;

        if (handler instanceof HandlerMethod handlerMethod) {
            log.info("[Interceptor] Controller 종료: {}.{} - {}ms",
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName(),
                    duration);
        }
    }
}
