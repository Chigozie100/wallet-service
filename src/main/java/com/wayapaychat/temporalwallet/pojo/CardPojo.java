package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class CardPojo {
	
	private String amount;
	
	private String app_ref;
	
	private String email;
	
	private String walletAcctNo;

	public CardPojo(String amount, String app_ref, String email, String walletAcctNo) {
		super();
		this.amount = amount;
		this.app_ref = app_ref;
		this.email = email;
		this.walletAcctNo = walletAcctNo;
	}
	
	

}
