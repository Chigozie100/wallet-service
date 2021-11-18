package com.wayapaychat.temporalwallet.proxy;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;

@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = WalletClientConfiguration.class)
public interface AuthProxy {

    
    @PostMapping("/auth/validate-user")
	public TokenCheckResponse getUserDataToken(@RequestHeader("authorization") String token);

    @PostMapping("/auth/validate-user")
    TokenCheckResponse getSignedOnUser();

}
