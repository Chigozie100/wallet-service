package com.wayapaychat.temporalwallet.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;


@Configuration
public class Configurations {


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
