package com.team23;

import com.team23.common.config.AsyncProperties;
import com.team23.common.config.CookieProperties;
import com.team23.common.config.CorsProperties;
import com.team23.common.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        CookieProperties.class,
        CorsProperties.class,
        AsyncProperties.class
})
public class EcommerceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceBackendApplication.class, args);
    }

}
