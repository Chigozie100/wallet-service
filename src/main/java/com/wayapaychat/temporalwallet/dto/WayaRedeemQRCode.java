package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WayaRedeemQRCode {
	
	private BigDecimal amount;
	
	private String referenceNo;
	
	private Long payerId;
	
	private String transactionCategory = "TRANSFER";
	private String receiverName;
	private String senderName;

	@NotBlank(message = "Payer Profile Id can not be Null or Blank")
	private String payerProfileId;

	@NotBlank(message = "Payee Profile Id can not be Null or Blank")
	private String payeeProfileId;

}
