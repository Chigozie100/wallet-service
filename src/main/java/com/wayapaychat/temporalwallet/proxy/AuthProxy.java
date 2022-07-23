package com.wayapaychat.temporalwallet.proxy;


import com.wayapaychat.temporalwallet.dto.TokenData;
import com.wayapaychat.temporalwallet.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.dto.OTPResponse;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;

import java.util.HashMap;

@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = WalletClientConfiguration.class)
public interface AuthProxy {

    
    @PostMapping("/auth/validate-user")
	public TokenCheckResponse getUserDataToken(@RequestHeader("authorization") String token);

    @PostMapping("/auth/validate-user")
    TokenCheckResponse getSignedOnUser();
    
    @PostMapping("/auth/verify-otp/transaction")
    OTPResponse postOTPVerify(@RequestBody WalletRequestOTP otp);
    
    @PostMapping("/auth/generate-otp/{emailOrPhoneNumber}")
    OTPResponse postOTPGenerate(@PathVariable("emailOrPhoneNumber") String emailOrPhoneNumber);



    @PostMapping("/auth/login")
    TokenCheckResponse getToken(@RequestBody HashMap request);
}
