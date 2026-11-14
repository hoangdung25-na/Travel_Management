package com.travel.ai.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

/**
 * Cấu hình Network & Timeout cho kết nối HTTP tới Google Gemini / OpenAI.
 * Đảm bảo thời gian chờ kết nối tối đa 10s và thời gian chờ đọc phản hồi tối đa 45s.
 */
@Configuration
public class AiClientConfig {

    @Bean
    public RestClientCustomizer aiRestClientCustomizer() {
        return restClientBuilder -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofSeconds(10));
            requestFactory.setReadTimeout(Duration.ofSeconds(45));
            restClientBuilder.requestFactory(requestFactory);
        };
    }
}
