package com.team23.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {
}