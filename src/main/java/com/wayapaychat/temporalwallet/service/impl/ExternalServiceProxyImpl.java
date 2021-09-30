package com.wayapaychat.temporalwallet.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.pojo.CardPojo;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.proxy.CardProxy;
import com.wayapaychat.temporalwallet.proxy.ContactProxy;

@Service
public class ExternalServiceProxyImpl {
	
	@Autowired
	private CardProxy cardProxy;
	
	@Autowired
	private ContactProxy contactProxy;
	
	public ResponseEntity<?> getCardPayment(HttpServletRequest req, CardRequestPojo card, Long userId) {
		String token = req.getHeader(SecurityConstants.HEADER_STRING);
		if(card.getType().equals("CARD")) {
			Map<String, String> kCard = new HashMap<>();
			kCard.put("amount", card.getAmount());
			kCard.put("cardNumber", card.getCardNo());
			kCard.put("ref", card.getReference());
			kCard.put("userId", userId.toString());
			kCard.put("walletAccountNo", card.getWalletAccounttNo());
			ResponseEntity<?> resp = cardProxy.cardPayment(kCard, token);
			return resp;
		}else if(card.getType().equals("BANK")) {
			CardPojo vCard = new CardPojo(card.getAmount(), card.getReference(), card.getEmail(), card.getWalletAccounttNo());
			ResponseEntity<?> resp = cardProxy.payCheckOut(vCard,userId,token);
			return resp;
		}else if(card.getType().equals("LOCAL")) {
			ResponseEntity<?> resp = contactProxy.localTransfer(card.getSenderAccountNo(), card.getBenefAccountNo(), userId, card.getAmount(), token);
			return resp;
		}
		return null;
	}

}
