package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class AdminLocalTransferDTO {
	
	@NotNull(message = "Admin User Id must be Entered")
	private Long userId;
	
	@NotBlank(message = "Account must not Null or Blank")
	@Size(min=10, max=10, message = "Account must be 10 digit")
	private String debitAccountNumber;
    
	//@NotNull(message = "Account must be 10 digit")
	@NotBlank(message = "Account must not Null or Blank")
	@Size(min=10, max=10, message = "Account must be 10 digit")
    private String benefAccountNumber;
   
	@NotNull
	@Min(value = (long) 0.01, message ="Amount must be greater than zero")
    private BigDecimal amount;
    
	//@NotNull
	@NotBlank(message = "tranType must not Null or Blank")
	@Size(min=3, max=10, message = "tranType can either be CASH,CARD,LOCAL,MONEY and BANK")
    private String tranType;
    
	//@NotNull
	@NotBlank(message = "tranCrncy must not Null or Blank")
	@Size(min=3, max=5, message = "tranCrncy must be 3 alphanumeric (NGN)")
    private String tranCrncy;
	
	//@NotNull
	@NotBlank(message = "tranNarration must not Null or Blank")
	@Size(min=5, max=50, message = "tranNarration must be aleast 5 alphanumeric")
    private String tranNarration;
	
	//@NotNull
	@NotBlank(message = "tranNarration must not Null or Blank")
	@Size(min=3, max=50, message = "paymentReference must be aleast 3 alphanumeric")
	private String paymentReference;

	@NotBlank(message = "Sender Name must not be Blank or Null")
	private String senderName;

	@NotBlank(message = "Receiver Name must not be Blank or Null")
	private String receiverName;

	private BigDecimal fee = BigDecimal.ZERO;
	private String transactionChannel; //WAYABANK,POS_TERMINAL,POS_COMMISSION,WEB_TERMINAL,WEB_COMMISSION;
}
