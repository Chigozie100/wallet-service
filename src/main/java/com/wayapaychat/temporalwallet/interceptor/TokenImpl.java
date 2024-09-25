package com.wayapaychat.temporalwallet.interceptor;

import com.wayapaychat.temporalwallet.config.SecurityConstants;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Service
@Slf4j
public class TokenImpl {

	@Autowired
	private AuthProxy authProxy;

	@Autowired
	private Environment environment;

	public MyData getUserInformation(HttpServletRequest request) {
		MyData data = null;
		try {
			TokenCheckResponse tokenResponse = authProxy.getSignedOnUser(request.getHeader(SecurityConstants.HEADER_STRING),request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE));
			if (tokenResponse.isStatus())
				return tokenResponse.getData();

			log.info(tokenResponse.toString());
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

	public MyData getTokenUser(String token, HttpServletRequest request) {
		try {
			TokenCheckResponse tokenResponse = authProxy.getUserDataToken(token,request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE));
			log.info("Authorization Token: {}", tokenResponse);
			if (tokenResponse == null)
				throw new CustomException("UNABLE TO AUTHENTICATE", HttpStatus.BAD_REQUEST);

			if(tokenResponse.isStatus())
				return tokenResponse.getData();
			return null;
		} catch (Exception ex) {
			log.error("::Error getTokenUser {}", ex.getLocalizedMessage());
			ex.printStackTrace();
			return null;
		}
	}

	public String getToken() {

		try {
			HashMap<String, String> map = new HashMap();
			map.put("emailOrPhoneNumber", environment.getProperty("waya.service.username"));
			map.put("password", environment.getProperty("waya.service.password"));
			map.put("otp", "");
			String clientId = "WAYABANK";
			String clientType = "ADMIN";

			TokenCheckResponse tokenData = authProxy.getToken(map,clientId,clientType);
			return tokenData.getData().getToken();
		} catch (Exception ex) {
			log.error("Unable to get system token :: {}", ex);
			return null;
		}
	}

	public MyData getToken2() {

		try {
			HashMap<String, String> map = new HashMap();
			map.put("emailOrPhoneNumber", environment.getProperty("waya.service.username"));
			map.put("password", environment.getProperty("waya.service.password"));
			map.put("otp", "");
			String clientId = "WAYABANK";
			String clientType = "ADMIN";

			TokenCheckResponse tokenData = authProxy.getToken(map,clientId,clientType);
			return tokenData.getData();
		} catch (Exception ex) {
			log.error("Unable to get system token :: {}", ex);
			return null;
		}
	}

	public boolean validatePIN(String token, String pin,String clientId,String clientType) {
		try {
			HashMap<String, String> map = new HashMap();
			map.put("pin", pin);
			ApiResponse validaResponse = authProxy.validatePostPin(map, token,clientId,clientType);
			if (!validaResponse.getStatus()) {
				log.error("Unable to validate pin :: {}", validaResponse);
			}
			return validaResponse.getStatus();
		} catch (Exception ex) {
			log.error("Unable to validate pin :: {}", ex);
		}
		return false;
	}

}
