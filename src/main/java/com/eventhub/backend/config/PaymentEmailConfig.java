package com.eventhub.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.payment-email.enabled", havingValue = "true")
public class PaymentEmailConfig {
    @Bean(defaultCandidate = false)
    ThreadPoolTaskScheduler paymentEmailScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("payment-email-");
        return scheduler;
    }
}
