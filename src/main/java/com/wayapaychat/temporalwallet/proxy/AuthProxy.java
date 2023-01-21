package com.wayapaychat.temporalwallet.proxy;


import com.wayapaychat.temporalwallet.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.dto.OTPResponse;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;

import java.util.HashMap;

@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = WalletClientConfiguration.class)
public interface AuthProxy {

    @GetMapping("/pin/validate-pin/{pin}")
    ApiResponse validatePin(@PathVariable("pin") String pin, @RequestHeader("Authorization") String token);

    @PostMapping("/auth/validate-user")
	TokenCheckResponse getUserDataToken(@RequestHeader("Authorization") String token);

    @PostMapping("/auth/validate-user")
    TokenCheckResponse validateToken(@RequestHeader("Authorization") String token, @RequestHeader("url") String url);

    @PostMapping("/auth/validate-user")
    TokenCheckResponse getSignedOnUser();
    
    @PostMapping("/auth/verify-otp/transaction")
    OTPResponse postOTPVerify(@RequestBody WalletRequestOTP otp);
    
    @PostMapping("/auth/generate-otp/{emailOrPhoneNumber}")
    OTPResponse postOTPGenerate(@PathVariable("emailOrPhoneNumber") String emailOrPhoneNumber);

    @PostMapping("/auth/login")
    TokenCheckResponse getToken(@RequestBody HashMap request);
}
