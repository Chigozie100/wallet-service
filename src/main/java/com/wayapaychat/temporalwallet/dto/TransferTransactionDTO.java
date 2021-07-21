package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TransferTransactionDTO {
	
	@NotNull
	@Size(min=10, max=10)
	private String debitAccountNumber;
    
	@NotNull
	@Size(min=10, max=10)
    private String benefAccountNumber;
   
	@NotNull
    private BigDecimal amount;
    
	@NotNull
    private String tranType;
    
	@NotNull
	@Size(min=3, max=5)
    private String tranCrncy;
	
	@NotNull
	@Size(min=5, max=50)
    private String tranNarration;
	
	@NotNull
	@Size(min=3, max=50)
	private String paymentReference;

}
