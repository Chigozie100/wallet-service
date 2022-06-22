package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class BankPaymentDTO {
	
	@NotNull
	@Size(min=3, max=20)
	private String bankName;
    
	@NotNull
	@Size(min=10, max=10)
    private String customerAccountNumber;
   
	@NotNull
    private BigDecimal amount;
    
	@NotNull
	@Size(min=3, max=5)
    private String tranCrncy;
	
	@NotNull
	@Size(min=5, max=50)
    private String tranNarration;
	
	@NotNull
	@Size(min=3, max=50)
	private String paymentReference;
	
	@NotNull
	@Size(min=3, max=50)
	private String transactionCategory;

	private String senderName;

	private String receiverName;

}
