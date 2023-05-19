package com.wayapaychat.temporalwallet;

//import com.wayapaychat.temporalwallet.config.LoggableDispatcherServlet;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waya.security.auth.annotation.EnableWayaAuthAuditApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.web.client.RestTemplate;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableDiscoveryClient
@EnableWayaAuthAuditApi
@EnableFeignClients
@EnableScheduling
@EnableCaching
public class TemporalWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(TemporalWalletApplication.class, args);
	}


	@Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

//    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
//    public DispatcherServlet dispatcherServlet() {
//        return new LoggableDispatcherServlet();
//    }

//	@Value("${spring.redis.port}")
//	private int redisPort;
//
//	@Value("${spring.redis.host}")
//	private String redisHost;

	@Bean
	public ObjectMapper objectMapper(){
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
		return objectMapper;
	}

//	@Bean
//	public LettuceConnectionFactory redisConnectionFactory() {
//		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
//		return new LettuceConnectionFactory(config);
//	}
//
//	@Bean
//	public RedisTemplate<String, Object> redisTemplate() {
//		RedisTemplate<String, Object> template = new RedisTemplate<>();
//		template.setConnectionFactory(redisConnectionFactory());
//		return template;
//	}

}
