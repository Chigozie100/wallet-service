package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class AdminUserTransferDTO {
	
	@NotNull
	@Size(min=1, max=10)
	private String adminUserId;
    
	@NotNull
	@Size(min=10, max=10)
    private String customerAccountNumber;
   
	@NotNull
	@Min(value = 1, message ="Amount must be greater than zero")
    private BigDecimal amount;
    
	@NotNull
    private String tranType = "MONEY";
    
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
