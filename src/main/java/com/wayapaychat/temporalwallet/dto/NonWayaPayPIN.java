package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class NonWayaPayPIN {
		
//	@NotBlank(message = "Token must not Null or Blank")
//	@Size(min=3, max=20, message = "Token must be between 1 to 20 digit")
//    private String tokenId;
	
	@NotBlank(message = "PIN must not Null or Blank")
	@Size(min=3, max=10, message = "PIN must be between 1 to 10 digit")
    private String tokenPIN;
	
	@NotNull(message = "Beneficiary user info must not be Null or Blank")
    private String beneficiaryUserId;

//	@NotBlank(message = "Receiver Name can not be Null or Blank")
//	private String receiverName;
//
//	@NotBlank(message = "Sender Name can not be Null or Blank")
//	private String senderName;

}
