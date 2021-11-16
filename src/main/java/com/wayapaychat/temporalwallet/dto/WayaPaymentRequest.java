package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class WayaPaymentRequest {
	
	private PaymentRequest paymentRequest;
	
    private String command;

}
