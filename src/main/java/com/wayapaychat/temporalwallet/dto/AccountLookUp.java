package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class AccountLookUp {
	
	private Long vId;
	
	//private Long userId;
	
	//private String email;
	
	private String custName;
	
	private String vAccountNo;

	public AccountLookUp(Long vId, String custName, String vAccountNo) {
		super();
		this.vId = vId;
		this.custName = custName;
		this.vAccountNo = vAccountNo;
	}

}
