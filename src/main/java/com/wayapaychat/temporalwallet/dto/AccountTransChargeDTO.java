package com.wayapaychat.temporalwallet.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AccountTransChargeDTO {

	private Date transactionDate;

	private String tranType;

	private Date transactionTime;

	private String processorEmail;

	private String senderEmail;

	private String debitPhoneNo;

	private String debitAccountNo;

	private double debitTranAmount;

	private String tranNarration;

	private String debitCredit;

	private String tranId;
	
	private String creditAccountNo;
	
	private double creditTranAmount;
	
	private String receiverEmail;
	
	private String creditPhoneNo;
	
	private String creditDebit;
	
	private double chargeTranAmount;

	public AccountTransChargeDTO(Date transactionDate, String tranType, Date transactionTime, String processorEmail,
			String senderEmail, String debitPhoneNo, String debitAccountNo, double debitTranAmount,
			String tranNarration, String debitCredit, String tranId, String creditAccountNo, double creditTranAmount,
			String receiverEmail, String creditPhoneNo, String creditDebit, double chargeTranAmount) {
		super();
		this.transactionDate = transactionDate;
		this.tranType = tranType;
		this.transactionTime = transactionTime;
		this.processorEmail = processorEmail;
		this.senderEmail = senderEmail;
		this.debitPhoneNo = debitPhoneNo;
		this.debitAccountNo = debitAccountNo;
		this.debitTranAmount = debitTranAmount;
		this.tranNarration = tranNarration;
		this.debitCredit = debitCredit;
		this.tranId = tranId;
		this.creditAccountNo = creditAccountNo;
		this.creditTranAmount = creditTranAmount;
		this.receiverEmail = receiverEmail;
		this.creditPhoneNo = creditPhoneNo;
		this.creditDebit = creditDebit;
		this.chargeTranAmount = chargeTranAmount;
	}
	

}
