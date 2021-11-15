package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ReceiptRequest {
	
	private BigDecimal amount;
	
	private String amountInWords;
	
	private String receiverAccount;
	
	private String receiverBank;
	
	private String receiverName;
	
	private String referenceNumber;
	
	private String senderName;
	
	private Date transactionDate;
	
	private String transactionType;
	
	private String userId;

	public ReceiptRequest(BigDecimal amount, String amountInWords, String receiverAccount, String receiverBank,
			String receiverName, String referenceNumber, String senderName, Date transactionDate,
			String transactionType, String userId) {
		super();
		this.amount = amount;
		this.amountInWords = amountInWords;
		this.receiverAccount = receiverAccount;
		this.receiverBank = receiverBank;
		this.receiverName = receiverName;
		this.referenceNumber = referenceNumber;
		this.senderName = senderName;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.userId = userId;
	}
	

}
