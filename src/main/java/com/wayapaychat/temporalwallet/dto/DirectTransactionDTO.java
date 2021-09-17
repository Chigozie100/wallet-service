package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class DirectTransactionDTO {
	    
	    @NotNull
	    private Long vId;
	
		@NotBlank(message = "Virtual Account must not Null or Blank")
		@Size(min=10, max=10, message = "Virual Account must be 10 digit")
	    private String vAccountNo;
	   
		@NotNull
		@Min(value = 1, message ="Amount must be greater than zero")
	    private BigDecimal amount;
	    
		@NotBlank(message = "tranType must not Null or Blank")
	    private String tranType;
	    
		@NotBlank(message = "tranCrncy must not Null or Blank")
		@Size(min=3, max=5, message = "tranCrncy must be 3 alphanumeric (NGN)")
	    private String tranCrncy;
		
		@NotBlank(message = "tranNarration must not Null or Blank")
		@Size(min=5, max=50, message = "tranNarration must be aleast 5 alphanumeric")
	    private String tranNarration;
		
		@NotBlank(message = "payment Reference must not Null or Blank")
		@Size(min=3, max=50, message = "paymentReference must be aleast 3 alphanumeric")
		private String paymentReference;
		
		@NotBlank(message = "Event ID must not Null or Blank")
	    private String eventId;
		
		@NotBlank(message = "Secure Key must not Null or Blank")
	    private String secureKey;

}
