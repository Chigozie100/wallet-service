package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankPaymentDTO {
	
	@NotNull
	@Size(min=3, max=50)
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

	@NotBlank(message = "Sender Name must not be Null or Blank")
	private String senderName;

	@NotBlank(message = "Receiver Name must not be Null or Blank")
	private String receiverName;

	private String eventId;



}
