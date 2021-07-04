package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class WalletConfigDTO {
	
	@NotNull
	@Size(min=5, max=50)
	private String codeName;
	
	@NotNull
	@Size(min=5, max=50)
	private String codeDesc;
	
	@NotNull
	@Size(min=3, max=10)
	private String codeValue; 
	
	private String codeSymbol; 

}
