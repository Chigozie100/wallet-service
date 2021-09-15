package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class CardRequestPojo {

	private String amount;

	private String reference;

	private String email;

	private String walletAccounttNo;
	
	private String cardNo;
	
	private String benefAccountNo;
	
	private String senderAccountNo;
	
	private String type;

}
