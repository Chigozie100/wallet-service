package com.wayapaychat.temporalwallet.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenImpl {
	
	@Autowired
	private AuthProxy authProxy;
	
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

}
