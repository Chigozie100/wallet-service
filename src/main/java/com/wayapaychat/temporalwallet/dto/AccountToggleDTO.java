package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AccountToggleDTO {
	
	@NotNull
	private Long userId;
	
	@NotBlank
	private String newDefaultAcctNo;
	

}
