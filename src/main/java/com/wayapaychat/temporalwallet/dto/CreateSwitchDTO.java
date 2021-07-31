package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateSwitchDTO {
	
	@NotBlank
	@Size(min=3, max=20)
	private String switchCode;
	
	@NotBlank
	@Size(min=3, max=20)
	private String switchIdentity;

}
