package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class NonWayaPaymentDTO {
	
	/*@NotNull
	@Size(min=10, max=10)
    private String customerDebitAccountNo;
   
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
	*/
	
	@NotBlank(message = "Customer Account must not Null or Blank")
	@Size(min=10, max=10, message = "Customer Account must be 10 digit")
    private String customerDebitAccountNo;
	
	@NotBlank(message = "EmailOrPhone must not be Null or Blank")
	@Size(min=1, max=50, message = "EmailOrPhone must be between 1 to 50 digit")
    private String emailOrPhoneNo;
	
	@NotBlank(message = "FullName must not be Null or Blank")
	@Size(min=1, max=50, message = "FullName must be between 1 to 50 digit")
    private String fullName;
   
	@NotNull
	@Min(value = 100, message ="Amount must be greater or equal to 100")
    private BigDecimal amount;
    
	@NotBlank(message = "tranCrncy must not Null or Blank")
	@Size(min=3, max=5, message = "tranCrncy must be 3 alphanumeric (NGN)")
    private String tranCrncy;
	
	@NotBlank(message = "tranNarration must not Null or Blank")
	@Size(min=5, max=50, message = "tranNarration must be atleast 5 alphanumeric")
    private String tranNarration;
	
	@NotBlank(message = "payment Reference must not Null or Blank")
	@Size(min=3, max=50, message = "paymentReference must be aleast 3 alphanumeric")
	private String paymentReference;

}
