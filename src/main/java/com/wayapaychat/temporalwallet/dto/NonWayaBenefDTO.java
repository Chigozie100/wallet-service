package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class NonWayaBenefDTO {

	@NotBlank(message = "Beneficiary user info must not Null or Blank")
	private String beneficiaryUserId;

	@NotBlank(message = "Beneficiary Profile Info must not Null or Blank")
	private String beneficiaryProfileId;

	@NotNull
	@Min(value = 100, message ="Amount must be greater or equal to 1000")
    private BigDecimal amount;
    
	@NotBlank(message = "tranCrncy must not Null or Blank")
	@Size(min=3, max=5, message = "tranCrncy must be 3 alphanumeric (NGN)")
    private String tranCrncy;
	
	@NotBlank(message = "tranNarration must not Null or Blank")
	@Size(min=5, max=50, message = "tranNarration must be aleast 5 alphanumeric")
    private String tranNarration;
	
	@NotBlank(message = "payment Reference must not Null or Blank")
	@Size(min=3, max=50, message = "paymentReference must be aleast 3 alphanumeric")
	private String paymentReference;

	@NotBlank(message = "Sender Name can not be Blank or Null")
	private String senderName;

	@NotBlank(message = "Receiver Name can not be Blank or Null")
	private String receiverName;
}
