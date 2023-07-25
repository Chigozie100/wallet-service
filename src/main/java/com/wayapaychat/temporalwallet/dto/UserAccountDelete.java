package com.wayapaychat.temporalwallet.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserAccountDelete {
	
	private Long userId;
	@NotBlank(message = "Profile Id can not be Null or Blank")
	private String profileId;
	
	@JsonIgnore
	private boolean isUser = true;

}
