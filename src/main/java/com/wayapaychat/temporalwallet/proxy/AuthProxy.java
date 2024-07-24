package com.wayapaychat.temporalwallet.proxy;


import com.waya.security.auth.pojo.SecureConstants;
import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.pojo.UserDtoResponse;
import com.wayapaychat.temporalwallet.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.dto.OTPResponse;
import com.wayapaychat.temporalwallet.dto.UserProfileResponse;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;

import java.util.HashMap;

@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = WalletClientConfiguration.class)
public interface AuthProxy {

    @GetMapping("/pin/validate-pin/{pin}")
    ApiResponse validatePin(@PathVariable("pin") String pin, @RequestHeader("Authorization") String token,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @PostMapping("/pin/validate-pin")
    ApiResponse validatePostPin(@RequestBody HashMap request, @RequestHeader("Authorization") String token,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @PostMapping("/auth/validate-user")
	TokenCheckResponse getUserDataToken(@RequestHeader("Authorization") String token, @RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @PostMapping("/auth/validate-user")
    TokenCheckResponse validateToken(@RequestHeader("Authorization") String token, @RequestHeader("url") String url,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @PostMapping("/auth/validate-user")
    TokenCheckResponse getSignedOnUser(@RequestHeader("Authorization") String token,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);
    
    @PostMapping("/auth/verify-otp/transaction")
    OTPResponse postOTPVerify(@RequestBody WalletRequestOTP otp,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);
    
    @PostMapping("/auth/generate-otp/{emailOrPhoneNumber}")
    OTPResponse postOTPGenerate(@PathVariable("emailOrPhoneNumber") String emailOrPhoneNumber,@RequestParam(name = "businessId",required = false) String businessId,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @PostMapping("/auth/login")
    TokenCheckResponse getToken(@RequestBody HashMap request,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);
    
    @GetMapping("/user/{id}")
    UserDtoResponse getUserById(@PathVariable("id") long id, @RequestHeader("Authorization") String token, @RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @GetMapping("/profile/{userId}")
    UserProfileResponse getUserProfileByUserId(@PathVariable("userId") long id, @RequestHeader("Authorization") String token,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

    @GetMapping("/profile/{userId}/{profileId}")
    UserProfileResponse getProfileByIdAndUserId(@PathVariable("userId") long userId, @PathVariable String profileId, @RequestHeader("Authorization") String token,@RequestHeader(SecurityConstants.CLIENT_ID) String clientId, @RequestHeader(SecurityConstants.CLIENT_TYPE) String clientType);

}
