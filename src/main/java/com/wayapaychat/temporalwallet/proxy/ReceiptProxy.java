package com.wayapaychat.temporalwallet.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.dto.ReceiptJson;
import com.wayapaychat.temporalwallet.dto.ReceiptRequest;

@FeignClient(name = "${waya.receipt.service}", url = "${waya.receipt.receipturl}")
public interface ReceiptProxy {
	
	@PostMapping("/api/v1/receipts")
	ReceiptJson receiptOut(@RequestBody ReceiptRequest receipt, @RequestHeader("authorization") String token);
	

}
