package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class AdminAccountRestrictionDTO {
	
	@NotBlank(message = "Account must not be Null or Blank")
	@Size(min=10, max=10, message = "Account must be 10 digit")
	private String customerAccountNumber;
	
	@NotNull
	private boolean isAcctfreez;
	
//	@NotBlank(message = "FreezCode must not Null or Blank")
//	@Size(min=1, max=1, message = "Freeze can either be D=DEBIT FREEZE,C=CREDIT FREEZE and T= TOTAL FREEZE")
	private String freezCode; //Pause = DEBIT/CREDIT Block =TOTAL FREEZ
	
	//@NotBlank(message = "FreezReason must not be Null or Blank")
	//@Size(min=10, max=50, message = "FreezReason must be aleast 10 character")
	private String freezReason;
	
	@NotNull
	private boolean isAcctClosed;
	
	@NotNull
	private boolean isAmountRestrict;
	
	//@NotNull
	private BigDecimal lienAmount;
	
	//@NotBlank(message = "LienReason must not be Null or Blank")
	//@Size(min=10, max=50, message = "LienReason must be aleast 10 character")
	private String lienReason;

}
