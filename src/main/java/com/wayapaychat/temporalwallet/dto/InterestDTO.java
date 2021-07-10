package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class InterestDTO {
	
	@NotNull
	private String crncyCode;
	
	@NotNull
	@Size(min=5, max=10)
	private String interestCode;
	
	private boolean creditInterest;
	
	private boolean debitInterest;
	
	private double intRatePcnt;
	
    private double beginSlabAmt;
	
	private double endSlabAmt;
	
	private double penalIntPcnt;

}
