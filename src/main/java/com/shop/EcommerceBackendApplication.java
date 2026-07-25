package com.shop;

import com.shop.global.config.CookieProperties;
import com.shop.global.config.CorsProperties;
import com.shop.global.config.JwtProperties;
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
})
public class EcommerceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceBackendApplication.class, args);
    }

}
