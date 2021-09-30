package com.wayapaychat.temporalwallet.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AccountStatementDTO {
	
	private Date transactionDate;
	
	private String tranType;
	
	private Date transactionTime;
	
	private String senderEmail;
	
	private String receiverEmail;
	
	private String phoneNo; 
	
	private String accountNo;
	
	private double tranAmount;
	
	private String tranNarration;
	
	private String debitCredit;
	
	private String tranId;

	public AccountStatementDTO(Date transactionDate, String tranType, Date transactionTime, String senderEmail,
			String receiverEmail, String phoneNo, String accountNo, double tranAmount, String tranNarration,
			String debitCredit, String tranId) {
		super();
		this.transactionDate = transactionDate;
		this.tranType = tranType;
		this.transactionTime = transactionTime;
		this.senderEmail = senderEmail;
		this.receiverEmail = receiverEmail;
		this.phoneNo = phoneNo;
		this.accountNo = accountNo;
		this.tranAmount = tranAmount;
		this.tranNarration = tranNarration;
		this.debitCredit = debitCredit;
		this.tranId = tranId;
	}
	

}
