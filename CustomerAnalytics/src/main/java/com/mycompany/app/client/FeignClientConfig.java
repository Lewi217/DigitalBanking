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
                requestTemplate.header("X-Service-Name", "customerAnalytics-service");
                requestTemplate.header("X-Internal-Call", "true");
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

