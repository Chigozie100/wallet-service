package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class AccountLienDTO {
	
	@NotBlank(message = "Account must not be Null or Blank")
	@Size(min=10, max=10, message = "Account must be 10 digit")
	private String customerAccountNo;
	
//	@JsonIgnore
	private boolean isLien;
	
	@NotNull
    private BigDecimal lienAmount;
		
	@NotBlank(message = "LienReason must not be Null or Blank")
	@Size(min=10, max=50, message = "LienReason must be atleast 10 character")
	private String lienReason;

}
