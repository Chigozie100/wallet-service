//package com.wayapaychat.temporalwallet.config;
//
//import feign.RequestInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class WebhookClientConfig {
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            String webhookUrl = requestTemplate.headers().get("webhook-url").iterator().next();
//            requestTemplate.target(webhookUrl);
//            requestTemplate.header("webhook-url", webhookUrl);
//        };
//    }
//}
