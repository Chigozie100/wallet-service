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

	private String email;

	private String notifyEmail;

	private String phone;

	private String oneTimeTransactionLimit;

	public AccountSumary(Long uId, String custName, String accountNo, String debitLimit, String email,
			String notifyEmail, String phone) {
		this.uId = uId;
		this.custName = custName;
		this.accountNo = accountNo;
		this.debitLimit = debitLimit;
		this.email = email;
		this.notifyEmail = notifyEmail;
		this.phone = phone;
	}

	public AccountSumary(Long uId, String custName, String accountNo, String debitLimit, String email,
			String notifyEmail, String phone, String oneTimeTransactionLimit) {
		this.uId = uId;
		this.custName = custName;
		this.accountNo = accountNo;
		this.debitLimit = debitLimit;
		this.email = email;
		this.notifyEmail = notifyEmail;
		this.phone = phone;
		this.oneTimeTransactionLimit = oneTimeTransactionLimit;
	}

}
