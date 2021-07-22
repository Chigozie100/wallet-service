package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ProductDTO {
	
	@NotNull
	@Size(min=5, max=5)
	private String productCode;
	
	@NotNull
	private boolean sysGenerate;
	
	@NotNull
	private boolean paidInterest;
	
	@NotNull
	private boolean collectInterest;
	
	@NotNull
	private boolean staffEnabled;
	
	@NotNull
	@Size(min=1, max=10)
	private String frequency;
	
	@NotNull
	private boolean paidCommission;
	
	@NotNull
	@Size(min=5, max=10)
	private String interestCode;
	
	@NotNull
	private double productMinBalance;
	
	private boolean chqAllowedFlg;
	
	@NotNull
	@Size(min=5, max=10)
	private String glCode;

}
