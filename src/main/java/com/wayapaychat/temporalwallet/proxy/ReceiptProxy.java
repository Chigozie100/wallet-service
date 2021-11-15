package com.wayapaychat.temporalwallet.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.dto.ReceiptRequest;
import com.wayapaychat.temporalwallet.pojo.CardResponse;

@FeignClient(name = "${waya.receipt.service}", url = "${waya.receipt.receipturl}")
public interface ReceiptProxy {
	
	@PostMapping("/receipts")
	ResponseEntity<CardResponse> receiptOut(@RequestBody ReceiptRequest receipt, @RequestHeader("authorization") String token);
	

}
