package com.shop.global.config;

import com.shop.order.service.OrderMetricService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderMetricConfig {
    @Bean
    public MeterBinder orderStatusGaugeBinder(OrderMetricService orderMetricService) {
        return registry -> {
            Gauge.builder(
                            "order.status.current",
                            orderMetricService,
                            OrderMetricService::getPendingOrderCount
                    )
                    .description("현재 결제 완료 및 확정 대기 주문 수")
                    .tag("status", "pending")
                    .register(registry);

            Gauge.builder(
                            "order.status.current",
                            orderMetricService,
                            OrderMetricService::getConfirmedOrderCount
                    )
                    .description("현재 판매자 확정 주문 수")
                    .tag("status", "confirmed")
                    .register(registry);

            Gauge.builder(
                            "order.status.current",
                            orderMetricService,
                            OrderMetricService::getCancelledOrderCount
                    )
                    .description("현재 취소 주문 수")
                    .tag("status", "cancelled")
                    .register(registry);
        };
    }
}
