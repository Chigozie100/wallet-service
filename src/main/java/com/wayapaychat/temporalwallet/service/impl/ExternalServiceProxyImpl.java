package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dto.ReceiptJson;
import com.wayapaychat.temporalwallet.dto.ReceiptRequest;
import com.wayapaychat.temporalwallet.pojo.CardPojo;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.proxy.CardProxy;
import com.wayapaychat.temporalwallet.proxy.ContactProxy;
import com.wayapaychat.temporalwallet.proxy.ReceiptProxy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExternalServiceProxyImpl {
	
	@Autowired
	private CardProxy cardProxy;
	
	@Autowired
	private ContactProxy contactProxy;
	
	@Autowired
	ReceiptProxy receiptProxy;
	
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
	
	public ReceiptJson printReceipt(BigDecimal amount, String receiverAccount, 
			String referenceNumber, Date transactionDate, String transactionType, 
			String userId, String name, String category, String token, String senderName) {
		try {


		ReceiptRequest receipt = new ReceiptRequest(amount, "", receiverAccount, "WALLET",
				name, referenceNumber, senderName, transactionDate, transactionType, category, userId);
		log.info("Receipt Request: {}", receipt.toString());
		ReceiptJson receiptResponse = receiptProxy.receiptOut(receipt, token);
			if(receiptResponse == null)
				return null;
			
			log.info("Receipt: {}", receiptResponse.toString());
			
			if (receiptResponse.isStatus())
				return receiptResponse;
		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
		}
		return null;
	}

}
