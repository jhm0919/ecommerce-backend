package com.team23.common.config;

import com.team23.customer.cart.service.CartMetricService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartMetricConfig {

    @Bean
    public MeterBinder cartGaugeBinder(CartMetricService cartMetricService) {
        return registry -> Gauge.builder(
                        "cart.status.current",
                        cartMetricService,
                        CartMetricService::getNonEmptyCartCount
                )
                .description("현재 비어있지 않은 장바구니 수")
                .tag("status", "non_empty")
                .register(registry);
    }
}
