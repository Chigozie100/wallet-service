package com.wayapaychat.temporalwallet.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class UserAccountDelete {
	
	private Long userId;
	
	@JsonIgnore
	private boolean isUser = true;

}
