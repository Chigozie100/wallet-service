package com.wayapaychat.temporalwallet.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;
import com.wayapaychat.temporalwallet.util.ApiResponse;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Service
@Slf4j
public class TokenImpl {
	
	@Autowired
	private AuthProxy authProxy;


	@Autowired
	private Environment environment;


	public MyData getUserInformation() {
		MyData data = null;
		try {
			TokenCheckResponse tokenResponse = authProxy.getSignedOnUser();
			if(tokenResponse.isStatus()) return tokenResponse.getData();
			
			log.info(tokenResponse.toString());
		}catch(Exception ex) {
			if(ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			data = null;
		}
		return data;
	}
	
	public MyData getTokenUser(String token) {
		MyData data = null;
		try {
			TokenCheckResponse tokenResponse = authProxy.getUserDataToken(token);
			if(tokenResponse == null)
				throw new CustomException("UNABLE TO AUTHENTICATE", HttpStatus.BAD_REQUEST);
			
			if (tokenResponse.isStatus())
				return tokenResponse.getData();

			log.info("Authorization Token: {}", tokenResponse.toString());
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			data = null;
		}
		return data;
	}

	public String getToken() {

		try {
			HashMap<String, String> map = new HashMap();
			map.put("emailOrPhoneNumber",  environment.getProperty("waya.service.username"));
			map.put("password", environment.getProperty("waya.service.password"));
			map.put("otp", "");


			TokenCheckResponse tokenData = authProxy.getToken(map);

			return tokenData.getData().getToken();

		} catch (Exception ex) {
			log.error("Unable to get system token :: {}", ex);
			throw new CustomException(ex.getMessage(), HttpStatus.NOT_FOUND);
		}
	}

    public boolean validatePIN(String token, String pin) {
		try {
			HashMap<String, String> map = new HashMap();
			map.put("pin", pin);
			ApiResponse validaResponse = authProxy.validatePostPin(map, token);

			if(!validaResponse.getStatus()){
				log.error("Unable to validate pin :: {}", validaResponse);
			}

			return validaResponse.getStatus();
		} catch (Exception ex) {
			log.error("Unable to validate pin :: {}", ex);
		}
        return false;
    }

}
