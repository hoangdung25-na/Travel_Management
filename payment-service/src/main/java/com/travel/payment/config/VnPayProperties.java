package com.travel.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Class cấu hình ánh xạ các thuộc tính VNPAY Sandbox từ application.yml
 * (payment.vnpay)
 */
@Configuration
@ConfigurationProperties(prefix = "payment.vnpay")
@Getter
@Setter
public class VnPayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String version = "2.1.0";
    private String command = "pay";
    private String currCode = "VND";
    private String locale = "vn";
}
