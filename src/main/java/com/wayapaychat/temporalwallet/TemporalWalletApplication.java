package com.wayapaychat.temporalwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
public class TemporalWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(TemporalWalletApplication.class, args);
	}

}
