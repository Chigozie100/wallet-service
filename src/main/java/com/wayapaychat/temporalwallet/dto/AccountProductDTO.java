package com.wayapaychat.temporalwallet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AccountProductDTO {
	
	private long userId;
	
	private String productCode;

	private String accountType;

	private String description;
	@NotBlank(message = "Profile Id can not be Blank or Null")
	private String profileId;

}
