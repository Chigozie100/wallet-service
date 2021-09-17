package com.wayapaychat.temporalwallet.proxy;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wayapaychat.temporalwallet.pojo.AccountResponse;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "${waya.account.service}", url = "${waya.account.accounturl}")
public interface AccountProxy {
	
	@GetMapping("/account/getAccounts")
	//ResponseEntity<AccountResponse> fetchAllVirtualAccount(@RequestHeader("authorization") String token);
	public ResponseEntity<AccountResponse> fetchAllVirtualAccount();
	
	@GetMapping("/account/getAccounts/{userId}")
	//ResponseEntity<AccountResponse> fetchVirtualAccount(@PathVariable("userId") Long userId, @RequestHeader("authorization") String token);
	public ResponseEntity<AccountResponse> fetchVirtualAccount(@PathVariable("userId") Long userId);
	
	class Configuration {

        @Bean
        Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
            return new SpringFormEncoder(new SpringEncoder(converters));
        }
       
    }

}
