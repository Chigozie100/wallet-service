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

	public TokenCheckResponse getUserData(String token) {
		TokenCheckResponse res = new TokenCheckResponse(null, false, "Failed to validat token", null);

		try {
			res = authProxy.getUserDataToken(token);
			log.info("::::Token::::" + res.getMessage());
			log.info("::::Token::::" + res.getData().getEmail());
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("FEIGN ERROR: {}", ex.getMessage());
		}
		return res;
	}

}
