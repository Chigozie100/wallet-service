package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class SecureDTO {
	
	@NotNull(message="Key must not be null")
	@Size(min=10, message = "Actual Key value must be entered")
	private String key;

}
