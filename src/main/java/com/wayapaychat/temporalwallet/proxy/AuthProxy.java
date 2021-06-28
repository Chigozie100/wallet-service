package com.wayapaychat.temporalwallet.proxy;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;

@FeignClient(name = "WAYA-AUTHENTICATION-SERVICE", url = "http://68.183.60.114:8059", configuration = WalletClientConfiguration.class)
public interface AuthProxy {

    
    @PostMapping("/auth/validate-user")
	public TokenCheckResponse getUserDataToken(@RequestHeader("authorization") String token);
    
    
}
