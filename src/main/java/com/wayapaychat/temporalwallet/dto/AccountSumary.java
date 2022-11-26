package com.wayapaychat.temporalwallet.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AccountSumary {
	
    private Long uId; 

	private String custName;
	
	private String accountNo;

	private String debitLimit;

	public AccountSumary(Long uId, String custName, String accountNo, String debitLimit) {
		this.uId = uId;
		this.custName = custName;
		this.accountNo = accountNo;
		this.debitLimit = debitLimit;
	}

}
