package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ProductCodeDTO {
	
	@NotNull
	@Size(min=5, max=5)
	private String product_code;
	
	@NotNull
	private String product_name; 
	
	@NotNull
	private String product_type;

}
