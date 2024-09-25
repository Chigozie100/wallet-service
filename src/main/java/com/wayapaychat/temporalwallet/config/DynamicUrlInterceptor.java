package com.wayapaychat.temporalwallet.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class DynamicUrlInterceptor implements RequestInterceptor {

    private String dynamicUrl;

    public void setDynamicUrl(String dynamicUrl) {
        this.dynamicUrl = dynamicUrl;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (dynamicUrl != null && !dynamicUrl.isEmpty()) {
            template.target(dynamicUrl);
        }
    }
}