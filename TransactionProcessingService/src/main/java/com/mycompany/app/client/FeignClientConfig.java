package com.mycompany.app.client;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor internalServiceInterceptor() {
        return requestTemplate -> {
            // For internal service calls, we don't forward auth headers
            log.debug("Making internal service call to: {}", requestTemplate.url());

            // Add service identification header for tracking
            requestTemplate.header("X-Service-Name", "account-service");
            requestTemplate.header("X-Internal-Call", "true");

            // Remove any existing Authorization header to prevent conflicts
            requestTemplate.removeHeader("Authorization");
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Slf4j
    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            log.error("Feign client error: {} - Status: {} - Reason: {}",
                    methodKey, response.status(), response.reason());

            if (response.status() == 403) {
                return new RuntimeException("Access denied to internal service. Check security configuration.");
            }
            if (response.status() == 404) {
                return new RuntimeException("User not found with the provided ID.");
            }

            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}