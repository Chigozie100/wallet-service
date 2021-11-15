package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WayaPaymentQRCode {
	
	private String name;
	
	private BigDecimal amount;
	
	private String reason;
	
	private Long payeeId;
	
	private String crncyCode;

}
