package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ProductCodeDTO {
	
	@NotNull
	@Size(min=5, max=5)
	private String productCode;
	
	@NotNull
	private String productName; 
	
	@NotNull
	private String productType;
	
	@NotNull
	private String currencyCode;

}
