package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class OfficeUserTransferDTO {
	
		@NotBlank(message = "Office Account must not Null or Blank")
		@Size(min = 12, max = 16, message = "Account must be 15 digit")
		private String officeDebitAccount;

		@NotBlank(message = "Customer Account must not Null or Blank")
		@Size(min = 10, max = 10, message = "Account must be 10 digit")
		private String customerCreditAccount;

		@NotNull
		@Min(value = (long) 0.01, message ="Amount must be greater than zero")
		private BigDecimal amount;

		@NotBlank(message = "tranType must not Null or Blank")
		private String tranType;

		@NotBlank(message = "tranCrncy must not Null or Blank")
		@Size(min = 3, max = 5, message = "tranCrncy must be 3 alphanumeric (NGN)")
		private String tranCrncy;

		@NotBlank(message = "tranNarration must not Null or Blank")
		@Size(min = 5, max = 50, message = "tranNarration must be aleast 5 alphanumeric")
		private String tranNarration;

		@NotBlank(message = "tranNarration must not Null or Blank")
		@Size(min = 3, max = 50, message = "paymentReference must be aleast 3 alphanumeric")
		private String paymentReference;

		@NotBlank(message = "Receiver name must not Null or Blank")
		private String receiverName;

		@NotBlank(message = "Sender name must not Null or Blank")
		private String senderName;

	public OfficeUserTransferDTO(String officeDebitAccount, String customerCreditAccount, BigDecimal amount,
								 String tranType,  String tranCrncy,  String tranNarration,  String paymentReference,
								 String receiverName, String senderName) {
		super();
		this.officeDebitAccount = officeDebitAccount;
		this.customerCreditAccount = customerCreditAccount;
		this.amount = amount;
		this.tranType = tranType;
		this.tranCrncy = tranCrncy;
		this.tranNarration = tranNarration;
		this.paymentReference = paymentReference;
		this.receiverName = receiverName;
		this.senderName = senderName;
	}

	public OfficeUserTransferDTO() {

	}
}
