package com.wayapaychat.temporalwallet;

import com.waya.security.auth.annotation.EnableWayaAuthAuditApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
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


}
