package com.team23.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 추후 Auditor 정보(누가 수정했는지) 등록 시 여기에 Bean 추가
}