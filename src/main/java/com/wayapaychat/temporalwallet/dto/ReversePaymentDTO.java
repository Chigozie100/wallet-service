package com.wayapaychat.temporalwallet.dto;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ReversePaymentDTO {
	
	@NotBlank(message = "Transaction ID must not be Null or Blank")
	@Size(min = 1, max = 20)
	private String tranId;

	@NotBlank(message = "tranCrncy must not be Null or Blank")
	@Size(min = 3, max = 5)
	private String tranCrncy;
	
	@NotNull
    private Date tranDate;
	
	@NotBlank(message = "Key must not be Null or Blank")
	private String secureKey;

}
