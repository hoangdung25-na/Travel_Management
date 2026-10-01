package com.travel.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cấu hình kích hoạt tính năng Spring Scheduled Poller cho Outbox Publisher
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
