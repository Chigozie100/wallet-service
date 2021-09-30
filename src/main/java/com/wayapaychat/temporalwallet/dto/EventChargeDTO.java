package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class EventChargeDTO {
	
	@NotNull
	@Size(min=6, max=50)
	private String eventId;
	
	private BigDecimal tranAmt;
	
	@NotNull
	@Size(min=8, max=10)
	private String placeholder;
	
	@NotNull
	@Size(min=3, max=5)
	private String crncyCode;
	
	@NotNull
	private boolean taxable;
	
	private BigDecimal taxAmt;
	
	@NotNull
	@Size(min=10, max=50)
	private String tranNarration;
	
	@NotNull
	private boolean chargeCustomer;
	
	@NotNull
	private boolean chargeWaya;

}
