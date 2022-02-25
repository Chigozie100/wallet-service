package com.wayapaychat.temporalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OTPResponse {
	
	private String timeStamp;
	private boolean status;
	private String message;

}
