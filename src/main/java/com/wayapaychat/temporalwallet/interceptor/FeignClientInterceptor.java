package com.wayapaychat.temporalwallet.interceptor;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

	private static final String AUTHORIZATION_HEADER = "Authorization";

	public static String getBearerTokenHeader() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) attrs).getRequest().getHeader(AUTHORIZATION_HEADER);
		}
		return null;
	}

	@Override
	public void apply(RequestTemplate requestTemplate) {
		String token = getBearerTokenHeader();
		
		Map<String, Collection<String>> headers = requestTemplate.request().headers();
        if(!ObjectUtils.isEmpty(token) && !headers.containsKey(AUTHORIZATION_HEADER)){
            requestTemplate.header(AUTHORIZATION_HEADER, token);
        }
	}
}