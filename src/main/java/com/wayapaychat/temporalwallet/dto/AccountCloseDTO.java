package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class AccountCloseDTO {
	
	@NotBlank(message = "Account must not be Null or Blank")
	@Size(min=10, max=10, message = "Account must be 10 digit")
	private String customerAccountNo;
	
	@JsonIgnore
	private boolean isClosure = true;

}
