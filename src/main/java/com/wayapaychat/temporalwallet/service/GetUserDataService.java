package com.wayapaychat.temporalwallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GetUserDataService {

	@Autowired
	private AuthProxy authProxy;


	public TokenCheckResponse getUserData(String token, String clientId, String clientType) {
		log.info("Getting user data with token: {}, clientId: {}, clientType: {}", token, clientId, clientType);
		TokenCheckResponse res = new TokenCheckResponse(null, false, "Failed to validate token", null);

		try {
			log.info("Sending request to authProxy for user data retrieval");
			res = authProxy.getUserDataToken(token, clientId, clientType);
			log.info("Received response from authProxy: {}", res);

			log.info("Token: {}", res.getMessage());
			if (res.getData() != null) {
				log.info("Email: {}", res.getData().getEmail());
			}
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status: {}", httpStatus);
			}
			log.error("FEIGN ERROR: {}", ex.getMessage());
		}
		return res;
	}


}
