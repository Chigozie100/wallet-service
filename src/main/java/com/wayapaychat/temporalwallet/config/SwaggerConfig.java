package com.wayapaychat.temporalwallet.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.models.auth.In;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
        		    .protocols(new HashSet<>(Arrays.asList("HTTP","HTTPs")))
                    .select()
                    .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                    .paths(PathSelectors.any())
                    .build()
                    .securitySchemes(Arrays.asList(new ApiKey("Token Access", HttpHeaders.AUTHORIZATION, In.HEADER.name())))
                    .securityContexts(Arrays.asList(securityContext()))
                    //.apiInfo(apiInfo())
                    .apiInfo(DEFAULT_API_INFO);

    }
    
    @SuppressWarnings("deprecation")
	private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.ant("/api/v1/**"))
            .build();
    
    }
    
    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("ADMIN", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("Token Access", authorizationScopes));
    }
    
   
    
    public static final Contact DEFAULT_CONTACT = new Contact("API Support", "https://www.wayapaychat.com",
			"admin@waya-paychat.com");
	
	public static final ApiInfo DEFAULT_API_INFO = new ApiInfo("TEMPORAL-WALLET-SERVICE REST API I",
			"RESTFUL TEMPORAL WALLET API DOCUMENTATION", "1.0", "urn:tos", DEFAULT_CONTACT, "Apache 2.0",
			"http://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
}

