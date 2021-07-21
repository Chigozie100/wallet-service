package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
@Data
public class EventPaymentDTO {
	
	@NotNull
	@Size(min=6, max=10)
	private String eventId;
    
	@NotNull
	@Size(min=10, max=10)
    private String customerAccountNumber;
   
	@NotNull
    private BigDecimal amount;
    
	@NotNull
	@Size(min=3, max=5)
    private String tranCrncy;
	
	@NotNull
	@Size(min=5, max=50)
    private String tranNarration;

}
