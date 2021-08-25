package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ChargeDTO {
	
	@NotBlank(message = "Charge Name must not be Null or Blank")
	@Size(min=1, max=50)
    private String chargeName;
	
    @NotBlank(message = "Currency must not be Null or Blank")
	@Size(min=3, max=5)
	private String currencyCode;
	
	@NotNull
	private double fixedAmount;
	
	@NotNull
	private double fixedPercent;
	
	@NotBlank(message = "Charge Per Mode must not Null or Blank")
	@Size(min = 3, max = 50, message = "Charge Mode can either be TRANSAC,DAILY,MONTHLY,QUATERLY,YEARLY")
	private String chargePerMode;
	//TRANSAC,DAILY,MONTH,QUATERLY, YEAR
	
	private boolean isTaxable;
	
	@NotBlank(message = "Charge Event must not Null or Blank")
	private String chargeEvent;

}
