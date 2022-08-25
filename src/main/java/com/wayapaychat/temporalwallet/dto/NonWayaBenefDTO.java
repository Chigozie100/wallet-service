package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class NonWayaBenefDTO {
	
	@NotBlank(message = "Merchant ID must not be Null or Blank")
	@Size(min=1, max=10, message = "Merchant ID must be between 1 to 10 digit")
    private Long merchantId;
   
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

	public NonWayaBenefDTO(
			Long merchantId,
			BigDecimal amount,
			String tranCrncy,
			String tranNarration,
			String paymentReference) {
		super();
		this.merchantId = merchantId;
		this.amount = amount;
		this.tranCrncy = tranCrncy;
		this.tranNarration = tranNarration;
		this.paymentReference = paymentReference;
	}
	

}
