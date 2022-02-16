package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WayaRedeemQRCode {
	
	private BigDecimal amount;
	
	private String referenceNo;
	
	private Long payerId;
	
	private String transactionCategory = "TRANSFER";

}
