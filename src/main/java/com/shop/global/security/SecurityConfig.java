package com.shop.global.security;

import com.shop.global.config.CorsProperties;
import com.shop.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

//    /**
//     * - 경로: /login/**
//     * - 세션 사용 (OAuth state 파라미터 저장 등 Spring 기본 동작에 맡김)
//     * - OAuth 성공 시 SuccessHandler가 JWT 발급 후 프론트로 리다이렉트
//     */
//    @Bean
//    @Order(1)
//    public SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/login/**")
//                .csrf(csrf -> csrf.disable())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                .authorizeHttpRequests(a -> a.anyRequest().permitAll());
//        return http.build();
//    }

    /**
     * Chain 2: API 전용 (완전한 Stateless, JWT only)
     * - 경로: 위에서 매칭 안 된 모든 요청 (주로 /api/**)
     * - 세션 절대 생성 안 함 → JWT만 유일한 인증 수단
     */
    @Bean
//    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 설정
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ★ Swagger 경로 허용 추가
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()
//                        .requestMatchers("/actuator/**").permitAll()
//                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(("/api/members/register")).permitAll()
                        .requestMatchers(("/api/auth/login")).permitAll()
                        .requestMatchers("/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
//                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/memebers/me/**").hasRole("USER")
                        .requestMatchers("/api/members/**").permitAll()
                        // ───── 어드민 - 판매자 입점 신청 ─────
//                        .requestMatchers("/api/admin/members/applications/**").permitAll()
                        // ───── 어드민 - 배너 관리 (추가) ─────
//                        .requestMatchers("/api/admin/banners/**").permitAll()
                        // ───── 어드민 - 대시보드 (추가) ─────
//                        .requestMatchers("/api/admin/dashboard/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 질문 등록/삭제 — 로그인 필요
//                        .requestMatchers("/api/questions/**").authenticated()
//                        .requestMatchers(HttpMethod.POST, "/api/products/*/questions").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                )
                // AuthenticationManager
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // UsernamePasswordAuthenticationFilter필터 앞에다가 jwtAuthenticationFilter필터를 놓겠다는 뜻
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.allowedOrigins());  // ★ 변경
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
