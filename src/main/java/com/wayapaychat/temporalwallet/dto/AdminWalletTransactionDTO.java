package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class AdminWalletTransactionDTO {
	
	@NotNull(message = "Admin User Id must not Null")
	private Long adminUserId;

	@NotBlank(message = "Profile Id must not Null or Blank")
	private String profileId;
	
	@NotBlank(message = "Account must not Null or Blank")
	@Size(min=10, max=10, message = "Account must be 10 digit")
	private String debitAccountNumber;
	    
	//@NotNull
	@NotBlank(message = "Email must not be Null or Blank")
	@Size(min=1, max=50)
    private String emailOrPhoneNumberOrUserId;
   
	@NotNull
	@Min(value = (long) 0.01, message ="Amount must be greater than zero")
    private BigDecimal amount;
    
	//@NotNull
	@NotBlank(message = "tranType must not be Null or Blank")
    private String tranType;
    
	//@NotNull
	@NotBlank(message = "tranCrncy must not be Null or Blank")
	@Size(min=3, max=5)
    private String tranCrncy;
	
	@NotNull
	@Size(min=5, max=50)
    private String tranNarration;
	
	@NotNull
	@Size(min=3, max=50)
	private String paymentReference;

	@NotBlank(message = "Receiver name must not Null or Blank")
	private String receiverName;

	@NotBlank(message = "Sender name must not Null or Blank")
	private String senderName;

}
