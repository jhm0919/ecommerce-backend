package com.team23.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public record CookieProperties(
        String refreshTokenName,
        boolean secure,
        String sameSite,
        String path,
        String domain,
        long maxAgeSeconds
) {
    // 기본값 제공 (설정 누락 방지)
    public CookieProperties {
        if (refreshTokenName == null) refreshTokenName = "refreshToken";
        if (sameSite == null) sameSite = "Lax";
        if (path == null) path = "/";
        if (maxAgeSeconds == 0) maxAgeSeconds = 14 * 24 * 60 * 60;  // 14일
    }
}