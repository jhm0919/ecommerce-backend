package com.team23.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(
        Pool application,
        Pool notification
) {
    public AsyncProperties {
        if (application == null) {
            application = new Pool(4, 8, 200, 30);
        }
        if (notification == null) {
            notification = new Pool(4, 10, 500, 30);
        }
    }

    public record Pool(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            int awaitTerminationSeconds
    ) {
        public Pool {
            if (corePoolSize <= 0) {
                corePoolSize = 4;
            }
            if (maxPoolSize < corePoolSize) {
                maxPoolSize = corePoolSize;
            }
            if (queueCapacity <= 0) {
                queueCapacity = 100;
            }
            if (awaitTerminationSeconds <= 0) {
                awaitTerminationSeconds = 30;
            }
        }
    }
}
