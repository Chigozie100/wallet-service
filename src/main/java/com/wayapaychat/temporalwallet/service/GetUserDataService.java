package com.wayapaychat.temporalwallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wayapaychat.temporalwallet.pojo.TokenCheckResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;




@Component
public class GetUserDataService {

	@Autowired
	private AuthProxy authProxy;

	public TokenCheckResponse getUserData(String token) {
		TokenCheckResponse res = authProxy.getUserDataToken(token);
		System.out.println("::::Token::::"+res.getMessage());
		System.out.println("::::Token::::"+res.getData().getEmail());
		return res;
	}

}
