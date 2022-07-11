package com.wayapaychat.temporalwallet;

//import com.wayapaychat.temporalwallet.config.LoggableDispatcherServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.web.client.RestTemplate;

import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class TemporalWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(TemporalWalletApplication.class, args);
	}
	
	@Bean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }
	@Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

//    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
//    public DispatcherServlet dispatcherServlet() {
//        return new LoggableDispatcherServlet();
//    }

}
