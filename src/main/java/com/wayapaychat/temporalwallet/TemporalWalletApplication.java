package com.wayapaychat.temporalwallet;

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

	  @Value("${spring.redis.port}")
 private int redisPort;

 @Value("${spring.redis.host}")
 private String redisHost;

	public static void main(String[] args) {
		SpringApplication.run(TemporalWalletApplication.class, args);
	}


	@Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

	  @Bean
 public LettuceConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
    return new LettuceConnectionFactory(config);
 }

 @Bean
 public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());
    return template;
 }

}
