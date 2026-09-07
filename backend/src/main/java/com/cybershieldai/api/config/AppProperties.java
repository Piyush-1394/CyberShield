package com.cybershieldai.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        String reportsDir,
        Razorpay razorpay,
        Mail mail,
        boolean seed
) {
    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {}
    public record Cors(String allowedOrigins) {}
    public record Razorpay(String keyId, String keySecret) {}
    public record Mail(String from) {}
}
