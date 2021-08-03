package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class AuthenticationDTO {
	
	private boolean status;
	
	private String message;
	
	AuthData data;

}
