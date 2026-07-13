package com.shop.global.config;

import com.shop.global.logging.PerformanceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final PerformanceInterceptor performanceInterceptor;

    // CORS에 대한 설정
    // http://localhost:3000 -> 8080 api를 호출할 수 있도록 설정

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**")           // API 만
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");  // Swagger는 예외
    }
}
